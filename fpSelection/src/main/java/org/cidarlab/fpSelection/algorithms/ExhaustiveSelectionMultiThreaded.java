/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cidarlab.fpSelection.dom.Cytometer;
import org.cidarlab.fpSelection.dom.Detector;
import org.cidarlab.fpSelection.dom.Fluorophore;
import org.cidarlab.fpSelection.dom.Laser;
import org.cidarlab.fpSelection.dom.SelectionInfo;
import org.cidarlab.fpSelection.selectors.ProteinSelector;
import org.cidarlab.fpSelection.dom.SNR;

/**
 *
 * @author Alex
 */
public class ExhaustiveSelectionMultiThreaded {

    public static AtomicInteger counter = new AtomicInteger(0);

    public LinkedList<int[]> filterCombinations;
    public LinkedList<int[]> fluorophorePermutations;
    public Fluorophore[] fluorophores;
    public Laser[] lasers;
    public Detector[] detectors;

    public double[][] filterSignal;

    public volatile int computationIndex = 0;
    public int onePercent = 0;

    public synchronized void syncPercent() {
        if (++computationIndex % onePercent == 0) {
            System.out.println(computationIndex / onePercent + " percent");
        }
    }

    public List<SelectionInfo> run(int n, Map<String, Fluorophore> spectralMaps, Cytometer cytometer, int threads) throws IOException, InterruptedException, ExecutionException {

        int numFluorophores = spectralMaps.size();

        //System.out.println("Number of Fluorophores :: " + numFluorophores);
        int numFilters = 0;
        for (Laser laser : cytometer.lasers) {
            numFilters += laser.detectors.size();
        }
        //System.out.println("Number of Filters      :: " + numFilters);
        
        //fluorophore index --> fluorophore object
        fluorophores = new Fluorophore[numFluorophores];
        int fpi = 0;
        for (Map.Entry<String, Fluorophore> entry : spectralMaps.entrySet()) {
            Fluorophore fluorophore = entry.getValue();
            fluorophores[fpi] = fluorophore;
            fpi++;
        }

        filterSignal = new double[numFilters][numFluorophores];
        //filter index --> laser
        lasers = new Laser[numFilters];
        //filter index --> detector
        detectors = new Detector[numFilters];
        int filterIndex = 0;
        for (Laser laser : cytometer.lasers) {
            for (Detector detector : laser.detectors) {
                lasers[filterIndex] = laser;
                detectors[filterIndex] = detector;
//                for (int i = 0; i < fluorophores.length; i++) {
//                    Fluorophore fluorophore = fluorophores[i];
//                    filterSignal[filterIndex][i] = fluorophore.express(laser, detector);
//                }
//                for (Map.Entry<String, Fluorophore> entry : spectralMaps.entrySet()) {
//                    Fluorophore fluorophore = entry.getValue();
//                    filterSignal[filterIndex][fluorophoreIndex] = fluorophore.express(laser, detector);
//                    fluorophoreIndex++;
//                }
                filterIndex++;
            }
        }
//        System.out.println("\nLASERS :: ");
//        for(int i=0;i<lasers.length;i++){
//            System.out.println(i + " :: " + lasers[i].name);
//        }
//        System.out.println("\nDetectors::");
//        for(int i=0;i<detectors.length;i++){
//            System.out.println(i + " :: " + detectors[i].identifier);
//        }
        //get all combinations of filters (order not important)
        filterCombinations = new LinkedList<>();
        int tempData[] = new int[n];
        getCombinations(tempData, 0, numFilters - 1, 0, n);

//        System.out.println("ALL COMBINATIONS");
//        for(int[] combinations: filterCombinations){
//            for(int i=0;i<combinations.length;i++){
//                System.out.print(combinations[i] + ",");
//            }
//            System.out.println("");
//        }
        //System.out.println("Number of filter combinations :: " + filterCombinations.size());
        //get all permutations of fluorophores to match to filters (order is important)
        fluorophorePermutations = new LinkedList<>();
        tempData = new int[n];
        getPermutations(tempData, numFluorophores, n);

//        System.out.println("ALL Permutations");
//        for(int[] permutations: fluorophorePermutations){
//            for(int i=0;i<permutations.length;i++){
//                System.out.print(permutations[i] + ",");
//            }
//            System.out.println("");
//        }
        //System.out.println("Number of fluorophore permutations :: " + fluorophorePermutations.size());
        //iterate through all possible combinations of filters/fluorophores
        int[] bestFilters = new int[n];
        int[] bestFluorophores = new int[n];
        int totalComputations = filterCombinations.size() * fluorophorePermutations.size();
        onePercent = (int) (totalComputations * .01);

        ExecutorService exec = Executors.newFixedThreadPool(threads);
        List<Future<BestResult>> resultList = new ArrayList<>();

        int chunkSize = filterCombinations.size() / threads;
        int start = 0;
        int finish = chunkSize;
        for (int i = 0; i < threads; i++) {
            Future<BestResult> result = exec.submit(new SignalThread(start, finish, n));
            resultList.add(result);
            start += chunkSize;
            finish += chunkSize;
            if (i == (threads - 2)) {
                finish = filterCombinations.size();
            }
        }
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        BestResult first = resultList.get(0).get();
        SNR bestSNR = first.bestSNR;
        List<SelectionInfo> bestSelection = first.bestSelection;

        for (Future<BestResult> result : resultList) {
            BestResult br = result.get();
            if(br.bestSNR.greaterThan(bestSNR)){
                bestSNR = br.bestSNR;
                bestSelection = new ArrayList<>(br.bestSelection);
            }
        }
        ProteinSelector.generateNoise(bestSelection);
        return bestSelection;
    }

    public void getCombinations(int data[], int start, int n, int index, int k) {
        if (index == k) {
            filterCombinations.add(data.clone());
            return;
        }
        for (int i = start; i <= n && n - i + 1 >= k - index; i++) {
            data[index] = i;
            getCombinations(data, i + 1, n, index + 1, k);
        }
    }

    public void getPermutations(int data[], int n, int k) {
        if (k == 0) {
            fluorophorePermutations.add(data.clone());
            return;
        }
        outerloop:
        for (int i = 0; i < n; ++i) {
            for (int j = data.length - 1; j >= k; j--) {
                if (data[j] == i) {
                    continue outerloop;
                }
            }
            data[k - 1] = i;
            getPermutations(data, n, k - 1);
        }
    }

    class SignalThread implements Callable<BestResult> {

        int start;
        int finish;
        int n;

        SignalThread(int start, int finish, int n) {
            this.start = start;
            this.finish = finish;
            this.n = n;
        }

        @Override
        public BestResult call() {
            BestResult bestResult = new BestResult();
            ArrayList<SelectionInfo> firstCandidate = assignCandidate(fluorophorePermutations.get(0), filterCombinations.get(start), n);
            bestResult.bestSelection = new ArrayList<>(firstCandidate);
            bestResult.bestSNR = new SNR(firstCandidate);
                    
            
            for (int i = start; i < finish; i++) {
                int[] filterCombo = filterCombinations.get(i);
                for (int[] fluorophorePerm : fluorophorePermutations) {
                    //syncPercent();
                    ArrayList<SelectionInfo> candidate = assignCandidate(fluorophorePerm, filterCombo, n);
                    SNR snr = new SNR(candidate);
                    
                    if(snr.greaterThan(bestResult.bestSNR)){
                        bestResult.bestSNR = snr;
                        bestResult.bestSelection = new ArrayList<>(candidate);
                    }
                }
            }
            return bestResult;
        }
    }

    

    private ArrayList<SelectionInfo> assignCandidate(int[] fluorophorePerm, int[] filterComb, int n) {
        ArrayList<SelectionInfo> candidate = new ArrayList<SelectionInfo>();

        //System.out.println("\nNEW SELECTION :: ");
        for (int i = 0; i < n; i++) {
            SelectionInfo si = new SelectionInfo();
            si.selectedFluorophore = fluorophores[fluorophorePerm[i]];
            si.selectedDetector = detectors[filterComb[i]];
            si.selectedLaser = lasers[filterComb[i]];
            candidate.add(si);
            //System.out.println(fluorophores[fluorophorePerm[i]].name + "," + lasers[filterComb[i]].name + "," + detectors[filterComb[i]].identifier);
        }

        return candidate;
    }

    private static String printFPPerumation(int[] fluorophorePerm, Fluorophore[] fluorophores) {
        String line = "";
        for (int i = 0; i < fluorophorePerm.length; i++) {
            line += fluorophores[fluorophorePerm[i]].name + ",";
        }
        return line;
    }

    class BestResult {

        SNR bestSNR;
        List<SelectionInfo> bestSelection;
        
        public BestResult() {
            bestSelection = new ArrayList<>();
        }

    }

}
