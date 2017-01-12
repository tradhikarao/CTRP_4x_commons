package com.fiveamsolutions.nci.commons.kmplot.util.dto;

import java.util.Collection;

/**
 * @author caIntegrator Team
 * This class serves as the final input for the KMPlot Service. It holds an
 * indsicriminate amoutn of KMSampleGroupCriteriaDTO, which, in turn, hold
 * a collection of KMSampleDTOs. These KMSampleDTOs are basically samples with coordinates
 * attached to them.
 *
 */

public class KMCriteriaDTO {
    private String plotTitle;
    private Collection<KMSampleGroupCriteriaDTO> sampleGroupCriteriaDTOCollection;
    private String probablityAxisLabel;
    private String durationAxisLabel;

    /**
     * Gets the probability axis label.
     * @return the probability axis label
     */
    public String getProbablityAxisLabel() {
        return probablityAxisLabel;
    }

    /**
     * Sets the probability axis label.
     * @param probablityAxisLabel the probabilityAxisLabel to set.
     */
    public void setProbablityAxisLabel(String probablityAxisLabel) {
        this.probablityAxisLabel = probablityAxisLabel;
    }

    /**
     * Gets the duration axis label.
     * @return the duration axis label
     */
    public String getDurationAxisLabel() {
        return durationAxisLabel;
    }

    /**
     * Sets the duration axis label.
     * @param durationAxisLabel the durationAxisLabel to set.
     */
    public void setDurationAxisLabel(String durationAxisLabel) {
        this.durationAxisLabel = durationAxisLabel;
    }

    /**
     * Default Constructor.
     */
    public KMCriteriaDTO() {
        //do nothing
    }

    /**
     * Get the plot title.
     * @return the plot title
     */
    public String getPlotTitle() {
        return plotTitle;
    }

    /**
     * Set the plot title.
     * @param plotTitle the plot title to set
     */
    public void setPlotTitle(String plotTitle) {
        this.plotTitle = plotTitle;
    }

    /**
     * Get the sample group criteria dto collection.
     * @return the sample group criteria dto collection
     */
    public Collection<KMSampleGroupCriteriaDTO> getSampleGroupCriteriaDTOCollection() {
        return sampleGroupCriteriaDTOCollection;
    }

    /**
     * Set the sample group criteria dto collection.
     * @param sampleGroupCriteriaDTOCollection the sample group criteria dto collection to set.
     */
    public void setSampleGroupCriteriaDTOCollection(
            Collection<KMSampleGroupCriteriaDTO> sampleGroupCriteriaDTOCollection) {
        this.sampleGroupCriteriaDTOCollection = sampleGroupCriteriaDTOCollection;
    }

}
