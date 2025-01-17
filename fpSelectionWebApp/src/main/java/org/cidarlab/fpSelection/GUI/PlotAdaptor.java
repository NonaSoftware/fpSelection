/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.GUI;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Graph;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import com.panayotis.gnuplot.terminal.SVGTerminal;
import org.apache.commons.io.IOUtils;
import org.cidarlab.fpSelection.dom.Cytometer;
import org.cidarlab.fpSelection.dom.Detector;
import org.cidarlab.fpSelection.dom.Fluorophore;
import org.cidarlab.fpSelection.dom.Laser;
import org.cidarlab.fpSelection.dom.SelectionInfo;

/**
 *
 * @author Alex
 */
public class PlotAdaptor {
    
    public static LinkedList<String> webPlot(List<SelectionInfo> info) throws IOException {
        //sort selection info so that filters with same lasers are plotted on same graph
        Collections.sort(info);

        //hash set that checks if lasers have been plotted already
        HashSet<Laser> usedLasers = new HashSet<>();

        //create the plot and initialize style/axes
        JavaPlot newPlot = new JavaPlot();
        Graph g;

        int width = 1200;
        int height = 800;

        newPlot.set("terminal", "png truecolor enhanced size 1200,600font 'arial,7'");
        newPlot.set("style fill", "transparent solid 0.3");
        newPlot.set("style data", "lines");
        newPlot.set("style data filledcurves", "x1");
        newPlot.set("key", "font ',8'");

        //if first plot to be added
        boolean first = true;
        boolean later = false;

        String selectionSummary = "";
        LinkedList<String> result = new LinkedList<>();
        double totalSNR = 0;
        int snrCount = 0;

        //if showing all lasers, filters, and fluorophores for "myFP"
        Cytometer cyto = info.get(0).myFPCytometer;
        if (cyto != null) {
            SelectionInfo entry = info.get(0);
            for (Laser laser : cyto.lasers) {
                g = new Graph();
                //add emission plot
//                for (Fluorophore fp : entry.selectedFluorophore) {
//                    PointDataSet EMDataSet = (fp.makeEMDataSet(laser));
//                    AbstractPlot emPlot = new DataSetPlot(EMDataSet);
//                    emPlot.setTitle(fp.name);
//                    if(first)newPlot.addPlot(emPlot);
//                    else g.addPlot(emPlot);
//                }

                Fluorophore fp = entry.selectedFluorophore;
                PointDataSet EMDataSet = (fp.makeEMDataSet(laser));
                AbstractPlot emPlot = new DataSetPlot(EMDataSet);
                emPlot.setTitle(fp.name);
                if (first) {
                    newPlot.addPlot(emPlot);
                } else {
                    g.addPlot(emPlot);
                }

                for (Detector detector : laser.detectors) {
                    //add filter bounds plot
                    PlotStyle ps = new PlotStyle(Style.LINES);
                    PointDataSet bounds = detector.drawBounds();
                    AbstractPlot boundsPlot = new DataSetPlot(bounds);
                    boundsPlot.setPlotStyle(ps);
                    boundsPlot.setTitle("");
                    if(first)newPlot.addPlot(boundsPlot);
                    else g.addPlot(boundsPlot);
                }
                if(first)
                {
                    newPlot.getAxis("x").setLabel("Wavelength (nm)'\r\nset title '" + laser.getName());
                    newPlot.getAxis("x").setBoundaries(300, 800);
                    newPlot.getAxis("y").setLabel("Intensity");
                    newPlot.getAxis("y").setBoundaries(0, 1.05);
                    first = false;
                }
                else
                {
                    g.getAxis("x").setLabel("Wavelength (nm)'\r\nset title '" + laser.getName());
                    g.getAxis("x").setBoundaries(300, 800);
                    g.getAxis("y").setLabel("Intensity");
                    g.getAxis("y").setBoundaries(0, 1.05);
                    newPlot.addGraph(g);
                }
            }
        }
        //oterwise, for normal operation iterate through laser/filter/protein combos
        else {
            for (SelectionInfo entry : info) {
                    
                g = new Graph();

                //add noise plot
                PointDataSet noiseDataSet = (entry.makeNoiseDataSet());
                AbstractPlot noisePlot = new DataSetPlot(noiseDataSet);
                noisePlot.setTitle("Aggregate Bleedthrough");
                noisePlot.set("fs", "transparent solid 0.2 noborder");

                //add emission plot
                Fluorophore fp = entry.selectedFluorophore;
                // FP ; Laser ; Filter ; Signal ; Bleedthrough
                double signal = fp.express(entry.selectedLaser, entry.selectedDetector);
                selectionSummary += entry.toString() + ", Signal = " + String.format("%.3f", (signal/100.0)) + ", Bleedthrough = [";
                for(SelectionInfo otherEntries:info){
                    if(otherEntries.selectedFluorophore.name != fp.name){
                        double bt = otherEntries.selectedFluorophore.express(entry.selectedLaser, entry.selectedDetector);                
                        selectionSummary += otherEntries.selectedFluorophore.name + " = " + String.format("%.3f", (bt/100.0)) + "; ";        
                    }
                }

                double bleedthrough = entry.expressNoise(entry.selectedDetector);
                selectionSummary += "]\r\n";
                snrCount++;

                PointDataSet EMDataSet = (fp.makeEMDataSet(entry.selectedLaser));
                AbstractPlot emPlot = new DataSetPlot(EMDataSet);
                emPlot.setTitle(fp.name);

                //add filter bounds plot
                PlotStyle ps = new PlotStyle(Style.LINES);
                PointDataSet bounds = entry.selectedDetector.drawBounds();
                AbstractPlot boundsPlot = new DataSetPlot(bounds);
                boundsPlot.setPlotStyle(ps);
                boundsPlot.setTitle("");

                //first round add to javaplot (otherwise extra plot is added)
                if (first) {
                    newPlot.addPlot(noisePlot);
                    newPlot.addPlot(emPlot);
                    newPlot.addPlot(boundsPlot);
                    newPlot.getAxis("x").setLabel("Wavelength (nm)'\r\nset title '" + String.format("%s (%s-%s)", entry.selectedFluorophore.name, entry.selectedLaser.getName(), entry.selectedDetector.identifier));
                    newPlot.getAxis("x").setBoundaries(300, 800);
                    newPlot.getAxis("y").setLabel("Intensity");
                    newPlot.getAxis("y").setBoundaries(0, 1.10);
                    first = false;
                    later = true;
                } //otherwise add to graph object that is added to javaplot
                else {
                    g = new Graph();
                    g.addPlot(noisePlot);
                    g.addPlot(emPlot);
                    g.addPlot(boundsPlot);
                    g.getAxis("x").setLabel("Wavelength (nm)'\r\nset title '" + String.format("%s (%s-%s)", entry.selectedFluorophore.name, entry.selectedLaser.getName(), entry.selectedDetector.identifier));
                    g.getAxis("x").setBoundaries(300, 800);
                    g.getAxis("y").setLabel("Intensity");
                    g.getAxis("y").setBoundaries(0, 1.10);
                    newPlot.addGraph(g);
                    later = false;
                }
            }
        }

        ImageTerminal png = new ImageTerminal();
        newPlot.setTerminal(png);
        newPlot.plot();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(png.getImage(), "jpg", baos);
        baos.flush();
        byte[] img = baos.toByteArray();
        baos.close();
        String baseString = "data:image/jpeg;base64, " + Base64.getEncoder().encodeToString(img);
        result.add(baseString);
        result.add(selectionSummary);

        return result;
    }

}
