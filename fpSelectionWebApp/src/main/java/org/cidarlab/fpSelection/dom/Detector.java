/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.dom;

import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author david
 */
public class Detector {
    
    @Getter
    @Setter
    private String identifier;
    
    @Getter
    @Setter
    private int channel;
    
    @Getter
    @Setter
    private int mirror;
    
    @Getter
    @Setter
    private int filterMidpoint;
    
    @Getter
    @Setter
    private int filterWidth;
    
    public Detector()
    {
        identifier = null;
        channel = 0;
        mirror = 0;
        filterMidpoint = 0;
        filterWidth = 0;
    }
    
    public PointDataSet drawBounds()
    {
        PointDataSet draw = new PointDataSet();
        int min = filterMidpoint - filterWidth/2;
        int max = filterMidpoint + filterWidth/2;
        
        for(int i = 0; i < 121; i++)
        {
            draw.add(new Point(min, i));
        }
        for(int j = min; j < max; j++)
        {
            draw.add(new Point(j,120));
        }
        for(int k = 120; k > 0; k--)
        {
            draw.add(new Point(max, k));
        }
        
        return draw;
        
    }
    
}