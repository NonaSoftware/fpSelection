/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.GUI;

/**
 *
 * @author david
 */
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.FillStyle;
import com.panayotis.gnuplot.style.PlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.swing.JPlot;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class javaplotSandbox {

    public static void main(String[] args) {
        //Path to gnuplot for Javaplot  -- NO LONGER NEEDED. instead of compiling from source I used sudo apt-get install and everything was fixed. I was missing dependencies
        //String gnuPlotPath = "/usr/local/bin/gnuplot";

        JavaPlot myPlot = new JavaPlot();

        //Default terminal has 0 use for you if your default terminal in gnuplot is set to unknown. 
        //But then again a lot of things in gnuplot would be broken if that is true.
        //If your default is set to something, then you will be A OK.
        System.out.println(myPlot.getTerminal());

        //Let's plot something simple
//        double[][] arr = new double[3][2];
//        arr[0][0] = -3;
//        arr[0][1] = 2;
//        arr[1][0] = -2;
//        arr[1][1] = 1;
//        arr[2][0] = 1.5;
//        arr[2][1] = 4;
//
//        double[][] arr2 = {
//            {3, 4},
//            {2, 3},
//            {1, 2},
//            {0, 1}
//        };
//
//        myPlot.addPlot(arr);
//        myPlot.addPlot(arr2);

        //Plot testing PointDataSet
        PointDataSet myDataSet = new PointDataSet();
        for(double i = -15; i < 15; i += .1)
        {
            myDataSet.add(new Point(i,1/(i*i+1)));
        }
        
        AbstractPlot data = new DataSetPlot(myDataSet);
        data.setTitle("");
        
        PlotStyle newStyle = new PlotStyle(Style.LINES);
        newStyle.setLineWidth(1);
        FillStyle fill = new FillStyle();
        fill.setStyle(FillStyle.Fill.SOLID);
        fill.setDensity(1);
        
        newStyle.setFill(fill);
        
        data.setPlotStyle(newStyle);
        
        
        
        myPlot.addPlot(data);
//        
//        myPlot.addPlot("sin(x)");

        //Commands for setting up dimensions and labeling.
        myPlot.setTitle("Test Plot", "Arial", 14);
        myPlot.getAxis("x").setLabel("Position");
        myPlot.getAxis("x").setBoundaries(-6, 6);
        myPlot.getAxis("y").setLabel("How often people trip");
        myPlot.getAxis("y").setBoundaries(-3, 5);

        //Bring plot up to screen and stay.
//        myPlot.setPersist(true);
//        myPlot.plot();
        
        JPlot plot = new JPlot(myPlot);
        plot.plot();
        plot.repaint();

        JFrame frame = new JFrame("My frame pls");
        frame.getContentPane().add(plot);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        //It seems to graphically display the JPlot, you would use paint(java.awt.Graphics g).
        //Is that an easy window into painting onto a GUI?
    }
}
