/*
 * Copyright (C) 2017 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 * 
 * Author: Marcel Stefko
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.alica.acpack.analyzers.quickpalm;

import ch.epfl.leb.alica.interfaces.Analyzer;
import ch.epfl.leb.alica.interfaces.analyzers.AnalyzerStatusPanel;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Produces a localization count per area using QuickPALM.
 * @author Marcel Stefko
 */
public class QuickPalm implements Analyzer {
    /**
     * Scaling factor for the density.
     * 
     * Multiplying the estimated density by a factor of 100 means that the
     * output returns localizations per 10 um x 10 um = 100 um^2 area.
     */
    private final double SCALEFACTOR = 100;
    
    private final QuickPalmCore core;
    private final ArrayList<Double> intermittent_outputs;
    
    private Roi roi;
    private double intermittent_output = 0.0;
    private int counter = 0;
    
    /**
     * Implementation of the QuickPALM algorithm as an analyzer, which produces
     * particle count as output.
     * @param live_view if true, live view of particle positions is shown
     */
    public QuickPalm(boolean live_view) {
        core = new QuickPalmCore(live_view);
        intermittent_outputs = new ArrayList<Double>();
    }
    
    @Override
    public void processImage(
            Object image,
            int image_width,
            int image_height,
            double pixel_size_um,
            long time_ms) {
        ImageProcessor sp = new ShortProcessor(image_width, image_height);
        sp.setPixels(image);
        
        double fov_area;
        if (roi == null) {
            fov_area = pixel_size_um * pixel_size_um *
                       image_width * image_height;
            
        } else {
            fov_area = pixel_size_um * pixel_size_um * 
                       roi.getBounds().getWidth() * roi.getBounds().getHeight();
            sp.setRoi(roi);
            sp = sp.crop();
        }
        synchronized(this) {
            intermittent_output = core.processImage(sp.duplicate(), counter++) /
                                  fov_area * SCALEFACTOR;
            intermittent_outputs.add(intermittent_output);
        }
    }

    @Override
    public double getIntermittentOutput() {
        return intermittent_output;
    }

    @Override
    public double getBatchOutput() {
        if (intermittent_outputs.size() == 0)
            return Double.NaN;
        double mean_output = 0.0;
        for (double d: intermittent_outputs) {
            mean_output += d;
        }
        mean_output /= intermittent_outputs.size();
        
        intermittent_outputs.clear();
        return mean_output;
    }
    
    @Override
    public void setROI(Roi roi) {
        this.roi = roi;
    }

    @Override
    public String getName() {
        return "QuickPALM";
    }
    
    @Override
    public void dispose() {
        core.dispose();
    }

    @Override
    public AnalyzerStatusPanel getStatusPanel() {
        return null;
    }
    
    @Override
    public String getShortReturnDescription() {
        String descr = "counts/" + String.valueOf((int) SCALEFACTOR) + "um^2";
        return descr;
    }
    
}
