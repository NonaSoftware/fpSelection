/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.selectors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cidarlab.fpSelection.algorithms.ExhaustiveSelection;
import org.cidarlab.fpSelection.algorithms.HillClimbingSelection;
import org.cidarlab.fpSelection.algorithms.SemiExhaustiveSelection;
import org.cidarlab.fpSelection.algorithms.SimulatedAnnealing;
import org.cidarlab.fpSelection.parsers.ScrapedCSVParse;
import org.cidarlab.fpSelection.parsers.fpFortessaParse;
import static org.cidarlab.fpSelection.parsers.fpSpectraParse.parse;
import org.cidarlab.fpSelection.dom.Cytometer;
import org.cidarlab.fpSelection.dom.Fluorophore;
import org.cidarlab.fpSelection.dom.SelectionInfo;

/**
 *
 * @author david
 */
public class API {
    
    //Not really necessary, but it's nice to have a centralized location for calling our functions.
    
    public static Map<String, Fluorophore> parseMasterList(File fpList) throws IOException
    {
        //to do
        Map<String, Fluorophore> returnList = parse(fpList);
        return returnList;
    }
    
    public static HashMap<String, Fluorophore> parseFPDir(File directory) throws IOException
    {
        //to do
        HashMap<String, Fluorophore> returnList = ScrapedCSVParse.parse(directory);
        return returnList;        
    }
    
    public static Cytometer parseCytometer(File csvFortessa, boolean filterSelection) throws IOException
    {
        Cytometer returnCyto = fpFortessaParse.parse(csvFortessa, filterSelection);
        return returnCyto;
    }
    
    public static List<SelectionInfo> exhaustiveSearch(int n, HashMap<String, Fluorophore> fps, Cytometer cyto) throws IOException
    {
        return ExhaustiveSelection.run(n, fps, cyto);
    }
    public static ArrayList<SelectionInfo> beamWidthSearch(int n, double width,  HashMap<String, Fluorophore> fps, Cytometer cyto) throws IOException
    {
        return SemiExhaustiveSelection.run(n, fps, cyto, width);
    }
    public static List<SelectionInfo> hillClimbSearch(int n, HashMap<String, Fluorophore> fps, Cytometer cyto) throws IOException
    {
        return HillClimbingSelection.run(n, fps, cyto);
    }
    public static List<SelectionInfo> simulatedAnnealSearch(int n, HashMap<String, Fluorophore> fps, Cytometer cyto) throws IOException
    {
        return SimulatedAnnealing.run(n, fps, cyto);
    }
    //For validating existing setups, provide a Hashmap of fluorophores chosen and your full cytometer data.
    public static List<SelectionInfo> validator(HashMap<String, Fluorophore> fps, Cytometer cyto) throws IOException
    {
        //By running it like this, it'll just suggest the strongest expressing filter-fp matchups, 
        //and the hill climbing portion will be skipped since we aren't clipping any fp's
        return HillClimbingSelection.run(fps.size(), fps, cyto);
    }
    
}
