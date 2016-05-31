/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.fpSelection.dom;

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
    
}