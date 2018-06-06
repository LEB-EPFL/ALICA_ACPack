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

import ch.epfl.leb.alica.interfaces.Analyzer;
import ch.epfl.leb.alica.interfaces.analyzers.AnalyzerStatusPanel;
import ch.epfl.leb.defcon.predictors.Predictor;
import ch.epfl.leb.defcon.predictors.SessionClosedException;
import ch.epfl.leb.defcon.predictors.ImageBitDepthException;
import ch.epfl.leb.defcon.predictors.NoLocalCountMapException;
import ch.epfl.leb.defcon.predictors.UninitializedPredictorException;
import ch.epfl.leb.defcon.predictors.internal.DefaultPredictor;

import ij.gui.Roi;
import ij.ImagePlus;
import ij.process.ShortProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A fluorescent spot counter derived from the DEFCoN package.
 * 
 * @author Kyle M. Douglass
 * @see <a href="https://github.com/LEB-EPFL/DEFCoN-ImageJ">DEFCoN-ImageJ</a>
 */
public class Defcon implements Analyzer {
    
    private final static Logger LOGGER = Logger.getLogger(Defcon.class.getName());
    
    /**
     * The square kernel size for computing the maximum local count.
     */
    private int boxSize;
    
    /**
     * The most recent spot count.
     */
    private double intermittentOutput = 0.0;
    
    /**
     * The count cache. This is emptied when getBatchOutput() is called.
     */
    private List<Double> intermittentOutputs;
    
    /**
     * Is the analyzer currently in live view mode?
     */
    private boolean liveMode;
    
    /**
     * A reference to the live view window.
     */
    private final ImagePlus liveView;
    
    /**
     * A flag indicating whether the total count or max local count is returned.
     */
    private boolean maxLocalCount;
    
    /**
     * The descriptive name for this analyzer.
     */
    private final String NAME = "DEFCoN";
    
    /**
     * The DEFCoN prediction machinery.
     */
    private final Predictor predictor;
    
    /**
     * The region of interest to analyze.
     */
    private Roi roi;
    
    /**
     * Scaling factor for the density.
     * 
     * Multiplying the estimated density by a factor of 100 means that the
     * output returns spots per 10 um x 10 um = 100 um^2 area.
     */
    private final double SCALE_FACTOR = 100;
    
    /**
     * The status panel for the DEFCoN analyzer.
     */
    private DefconStatusPanel statusPanel;
    
    /**
     * Initializes the DEFCoN analyzer.
     * 
     * @param pathToModel The path to the DEFCoN network model.
     */
    public Defcon(String pathToModel) {
        // Initialize the count cache.
        intermittentOutputs = new ArrayList<>();
        
        // Initializes the density map predictor.
        predictor = new DefaultPredictor();
        predictor.setup(pathToModel);
        
        // Creates the status panel.
        statusPanel = new DefconStatusPanel(this);
        
        // Setups the live view.
        liveMode = false;
        liveView = new ImagePlus("DEFCoN - Density Map");
        
        boxSize = 7;
        maxLocalCount = false;
    }
    
    /**
     * Cleans up the analyzer when it's finished.
     */
    @Override
    public void dispose() {
        predictor.close();
        if (this.liveView != null) {
            this.liveView.hide();
        }
    }
    
    /**
     * Failsafe in case the predictor has not been closed at the point of garbage collection.
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            predictor.close();
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Returns the averaged DEFCoN count value since the last call.
     * 
     * Double.NaN is returned if there is no new count since the previous call.
     * 
     * @return The averaged DEFCoN count.
     */
    @Override
    public double getBatchOutput() {
        if (intermittentOutputs.isEmpty())
            return Double.NaN;
        double meanOutput = 0.0;
        for (double d: intermittentOutputs) {
            meanOutput += d;
        }
        meanOutput /= intermittentOutputs.size();
        
        // Flush the output cache.
        intermittentOutputs.clear();
        
        return meanOutput;
    }
    
    /**
     * Returns the current square kernel size for maximum local counts.
     * 
     * @return The kernel size for computing the maximum local count.
     */
    public int getBoxSize() {
        return boxSize;
    }
    
    /**
     * Returns the intermittent output of the analyzer.
     * 
     * @return The analyzer's current output value.
     */
    @Override
    public double getIntermittentOutput() {
        return intermittentOutput;
    }
    
    /**
     * Returns the name of the DEFCoN analyzer.
     * 
     * @return The name of the DEFCoN analyzer.
     */
    @Override
    public String getName() {
        return NAME;
    }
    
    /**
     * Returns a short description of the values returned by the DEFCoN analyzer.
     * 
     * @return A short description of the values returned by the DEFCoN analyzer.
     */
    @Override
    public String getShortReturnDescription() {
        String descr = "";
        if (maxLocalCount) {
            descr = "maximum local count";
        } else {
            descr = "counts/" + String.valueOf((int) SCALE_FACTOR) + " um^2";
        }
            
        return descr;
    }
    
    /**
     * Returns the analyzer's status panel that will be displayed in the GUI.
     * 
     * If no panel is implemented, this method should return null. In this case, 
     * the corresponding space in the MonitorGUI will appear blank.
     *
     * @return The status panel of the DEFCoN analyzer or null.
     */
    @Override
    public AnalyzerStatusPanel getStatusPanel() {
        return statusPanel;
    }
    
    /**
     * True if live mode is on, false otherwise.
     * 
     * @return True if live mode is on, false otherwise
     */
    public boolean isLiveModeOn() {
        return liveMode;
    }
    
    /**
     * True if the analyzer is computing the maximum local count.
     * 
     * @return True if the analyzer is computing the maximum local count.
     */
    public boolean isMaxLocalCount() {
        return maxLocalCount;
    }
    
    /**
     * Turns on the live view of the density map.
     */
    public void liveModeOn() {
        synchronized(liveView) {
            liveMode = true;
        }
    }
    
    /**
     * Turns off the live view of the density map.
     */
    public void liveModeOff() {
        synchronized(liveView) {
            liveMode = false;
            liveView.hide();
        }
    }
    
    /**
     * Turns on the live view of the density map.
     */
    public void maxLocalCountOn() {
            // Flush the output cache.
            intermittentOutputs.clear();
            maxLocalCount = true;
    }
    
    /**
     * Turns off the live view of the density map.
     */
    public void maxLocalCountOff() {
            maxLocalCount = false;
    }
    
    /**
     * Processes an image and adjusts the analyzer's internal state to reflect the results of the calculation.
     * 
     * This method is called after each new image acquisition by the
     * AnalysisWorker. You can use the synchronized(this) statement within the
     * body of an implementation of an Analyzer to ensure that no output readout
     * happens during code execution.
     * 
     * @param image The image to be processed as 1D raw pixel data.
     * @param width Image width in pixels.
     * @param height Image height in pixels.
     * @param pixelSizeUm Length of a side of a square pixel in micrometers.
     * @param timeMs Image acquisition time in milliseconds.
     */
    @Override
    public void processImage(Object image, int width, int height,
                           double pixelSizeUm, long timeMs) {
        
        ShortProcessor sp = new ShortProcessor(width, height);
        sp.setPixels(image);
        
        double fovArea;
        if (roi == null) {
            fovArea = pixelSizeUm * pixelSizeUm * width * height;
        } else {
            fovArea = pixelSizeUm * pixelSizeUm *
                      roi.getBounds().getWidth() * roi.getBounds().getHeight();
            
        }
        sp.setRoi(roi);
        
        // Compute the density map.
        try {
            predictor.predict(sp.crop());
        } catch (ImageBitDepthException ex) {
            String msg = "The image must be either 16-bits or 8-bits.";
            LOGGER.log(Level.SEVERE, msg);
            LOGGER.log(Level.SEVERE, ex.getMessage());
        } catch (SessionClosedException ex) {
            String msg = "The TensorFlow session has been closed.";
            LOGGER.log(Level.SEVERE, msg);
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
        
        // Compute the spot density.
        synchronized(this) {
            try {
                if (maxLocalCount) {
                    intermittentOutput
                            = predictor.getMaximumLocalCount(boxSize);
                    intermittentOutputs.add(intermittentOutput);
                } else {
                    intermittentOutput = predictor.getCount() 
                                         / fovArea * SCALE_FACTOR;
                    intermittentOutputs.add(intermittentOutput);
                }
            } catch (UninitializedPredictorException ex) {
                String msg = "This predictor has not been initialized.";
                LOGGER.log(Level.SEVERE, msg);
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
         }
        
        // This should occur as the last call of this method so that the live
        // view is synced with the state of the analyzer.
        updateLiveView();
    }
    
    /**
     * Updates the live viewer.
     */
    public void updateLiveView() {
        synchronized(liveView) {
            if (liveMode) {
                try {
                    if (maxLocalCount) {
                        liveView.setProcessor(predictor.getLocalCountMap());
                    } else {
                        liveView.setProcessor(predictor.getDensityMap());
                    }
                } catch (UninitializedPredictorException ex) {
                    String msg = "Cannot update live view; " +
                                 "the predictor has not been initialized.";
                    LOGGER.log(Level.SEVERE, msg);
                } catch (NoLocalCountMapException ex) {
                    String msg = "Cannot update live view; " +
                                 "the max local count has not been computed.";
                    LOGGER.log(Level.SEVERE, msg);
                }
                liveView.updateAndDraw();
                
                if (!liveView.isVisible()) {
                    liveView.show();
                }
            }
        }
    }
    
    /**
     * Sets the square kernel size for computing the maximum local count.
     * 
     * @param boxSize The kernel size for computing the maximum local count.
     */
    public void setBoxSize(int boxSize) {
        if (boxSize % 2 == 0) {
            String msg = "boxSize must be odd. Received: " +
                         String.valueOf(boxSize);
            throw new IllegalArgumentException(msg);
        } else if (boxSize < 1) {
            String msg = "boxSize must be greater than 1. Recevied: " +
                         String.valueOf(boxSize);
            throw new IllegalArgumentException(msg);
        }
        this.boxSize = boxSize;
    }
    
    @Override
    public void setROI(Roi roi) {
        this.roi = roi;
    }
}
