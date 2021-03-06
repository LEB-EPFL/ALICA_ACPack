/**
 * Copyright (C) 2018 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne (EPFL), Switzerland
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.alica.acpack.analyzers.defcon;

import ij.IJ;
import ij.ImagePlus;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for the Defcon class.
 * 
 * @author Kyle M. Douglass
 */
public class DefconTest {
    
    private Defcon instance;
    private ImagePlus imp;
    private final String PATH_TO_MODEL = "tf_density_count";
    private final String STACK_FILE = "test_data.tif";
    
    @Before
    public void setUp() {
        String stackFile = DefconTest.class.getClassLoader()
                                           .getResource(STACK_FILE)
                                           .getPath();
        imp = IJ.openImage(stackFile);
        
        String modelPath = DefconTest.class.getClassLoader()
                                           .getResource(PATH_TO_MODEL)
                                           .getPath();
        
        instance = new Defcon(modelPath);
    }
    
    /**
     * Gets/sets the box size field for the maximum local count.
     */
    @Test
    public void testGetSetBoxSize() {
        // Default value
        int expResult = 7;
        int result = instance.getBoxSize();
        assertEquals(expResult, result);
        
        expResult = 5;
        instance.setBoxSize(expResult);
        result = instance.getBoxSize();
        assertEquals(expResult, result);
    }
    
    /**
     * Toggles the maximum local count feature.
     */
    @Test
    public void testMaxLocalCount() {
        // maxLocalCount is off by default
        assertEquals(false, instance.isMaxLocalCount());
        
        instance.maxLocalCountOn();
        assertEquals(true, instance.isMaxLocalCount());
        
        instance.maxLocalCountOff();
        assertEquals(false, instance.isMaxLocalCount());
        
    }
    
    /**
     * Ensures that the boxSize value is greater than 1.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBoxSize() {
        int boxSize = -5;
        instance.setBoxSize(boxSize);
    }
    
    /**
     * Ensures that the boxSize value is odd.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOddBoxSize() {
        int boxSize = 4;
        instance.setBoxSize(boxSize);
    }
    
    /**
     * Ensures that the processImage() method is called without errors.
     */
    @Test
    public void testProcessImage() {
        double pixelSizeUm = 0.1;
        long timeMs = 10;
        instance.processImage(imp.getProcessor().getPixels(), imp.getWidth(),
                              imp.getHeight(), pixelSizeUm, timeMs);
        
    }
    
    /**
     * Analyzer predicts the maximum local count instead of the density map.
     */
    @Test
    public void testProcessMaxLocalCount() {
        
        // Predict the full count
        double pixelSizeUm = 0.1;
        long timeMs = 10;
        instance.processImage(imp.getProcessor().getPixels(), imp.getWidth(),
                              imp.getHeight(), pixelSizeUm, timeMs);
        double fullCount = instance.getBatchOutput();
        
        // Predict the maximum local count
        instance.maxLocalCountOn();
        instance.processImage(imp.getProcessor().getPixels(), imp.getWidth(),
                              imp.getHeight(), pixelSizeUm, timeMs);
        double mlc = instance.getBatchOutput();
        
        assertNotEquals(fullCount, mlc, 0.0);
    }
}
