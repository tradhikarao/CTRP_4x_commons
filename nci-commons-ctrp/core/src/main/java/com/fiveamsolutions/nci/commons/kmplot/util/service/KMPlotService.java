package com.fiveamsolutions.nci.commons.kmplot.util.service;

import java.io.File;

import com.fiveamsolutions.nci.commons.kmplot.util.ImageTypes;
import com.fiveamsolutions.nci.commons.kmplot.util.KMException;
import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMCriteriaDTO;
import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMSampleGroupCriteriaDTO;


/**
 * @author caIntegrator Team
 */

public interface KMPlotService {

    /**
     * Create the plot as an image file.  Currently JPEG & PNG formats are supported.
     * @param outFile the output file
     * @param kmCrit the KM criteria DTO
     * @param imgType the type of image to create
     * @throws KMException on error
     */
    void createPlotAsImageFile(File outFile, KMCriteriaDTO kmCrit, ImageTypes imgType)
        throws KMException;

    /**
     * Write the plot to an ouput stream.
     * @param out the output stream
     * @param kmCrit the KM criteria DTO
     * @param imgType the type of image to create
     * @throws KMException on error
     */
    void writePlotToOutputStream(java.io.OutputStream out, KMCriteriaDTO kmCrit, ImageTypes imgType)
        throws KMException;

    /**
     * Create a buffered image of a KM plot.
     * @param kmCrit the KM criteria DTO
     * @return the buffered image of the KM plot
     */
    java.awt.image.BufferedImage createBufferedImage(KMCriteriaDTO kmCrit);

    /**
     * Compute Log-rank p-value, which indicates the significance of the difference in survival between
     * any two groups of samples.
     * @param group1 the first sample group
     * @param group2 the second sample group
     * @return the log-rank p-value.
     */
     Double computeLogRankPValueBetween(KMSampleGroupCriteriaDTO group1, KMSampleGroupCriteriaDTO group2);

     /**
      * Get the chart for a KM plot.
      * @param kmCrit the KM criteria DTO
      * @return the chart for the KM plot.
      */
     Object getChart(KMCriteriaDTO kmCrit);
}
