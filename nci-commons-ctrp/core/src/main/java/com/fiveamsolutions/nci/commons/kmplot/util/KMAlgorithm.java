package com.fiveamsolutions.nci.commons.kmplot.util;

import java.util.Collection;

import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMSampleDTO;
import com.fiveamsolutions.nci.commons.kmplot.util.model.XYCoordinate;

/**
 * @author caIntegrator Team
*/

public interface KMAlgorithm {

    /**
     * createPlot drawing data points for any set of sample.
     * @param sampleCollection the sample collection
     * @return the plot as a collection of data points
     */
    Collection<XYCoordinate> getPlottingCoordinates(Collection<KMSampleDTO> sampleCollection);

    /**
     * Compute the p-value between two sample series.
     * @param group1 the first sample series
     * @param group2 the second sample series
     * @return the p-value
     */
    Double getLogRankPValue(Collection<KMSampleDTO> group1, Collection<KMSampleDTO> group2);

    /**
     * unknown p-value.
     */
    Double UNKNOWN_PVALUE = -100.0;
}
