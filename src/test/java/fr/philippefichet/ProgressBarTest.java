/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glopinous
 */
public class ProgressBarTest {
    
    public ProgressBarTest() {
    }

    @Test
    public void test0Percent() {
        ProgressBar progressBar = new ProgressBar(0L, 100L, 20);
        assertEquals("[                  ]", progressBar.getProgressBar());
    }

    @Test
    public void test50Percent() {
        ProgressBar progressBar = new ProgressBar(50L, 100L, 20);
        assertEquals("[|||||||||         ]", progressBar.getProgressBar());
    }
    
    @Test
    public void test50PercentChangeSymbole() {
        ProgressBar progressBar = new ProgressBar(50L, 100L, 20);
        progressBar.setProgressSymbole('-');
        assertEquals("[---------         ]", progressBar.getProgressBar());
    }
    
    @Test
    public void test100Percent() {
        ProgressBar progressBar = new ProgressBar(100L, 100L, 20);
        assertEquals("[||||||||||||||||||]", progressBar.getProgressBar());
    }
}
