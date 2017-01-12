package com.fiveamsolutions.nci.commons.kmplot.util;

import java.util.Collection;

import com.fiveamsolutions.nci.commons.kmplot.util.model.GroupCoordinates;


/**
* @author caIntegrator Team
*/

public interface KMPlotter {

    /**
     * Write the image to the output stream.
     * @param out the output stream.
     * @param image the image.
     * @param imgType the image type
     * @throws KMException on error.
     */
    void writeBufferedImage(java.io.OutputStream out, java.awt.image.BufferedImage image, ImageTypes imgType)
            throws KMException;

    /**
     * Create the image.
     * @param groupsToBePlotted the groups to be plotted
     * @param plotName the plot name
     * @param durationAxisLabel the duration axis label
     * @param probablityAxisLabel the probability axis label
     * @return the image
     */
    java.awt.image.BufferedImage createImage(Collection<GroupCoordinates> groupsToBePlotted, String plotName,
            String durationAxisLabel, String probablityAxisLabel);

    /**
     * Create the image of known type.
     * @param groupsToBePlotted the groups to be plotted
     * @param plotName the plot name
     * @param durationAxisLabel the duration axis label
     * @param probablityAxisLabel the probability axis label
     * @return the image
     */
    Object createImageOfKnownType(Collection<GroupCoordinates> groupsToBePlotted, String plotName,
           String durationAxisLabel, String probablityAxisLabel);


}
