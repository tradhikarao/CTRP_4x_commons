package com.fiveamsolutions.nci.commons.kmplot.util.dto;

import java.awt.Color;
import java.util.Collection;

/**
 * @author caIntegrator Team
 * This class holds information about a group of samples such as the color
 * to be used in display, the name of the collective group and it stores the
 * actual collection of KMSampleDTOs itself.
 */

public class KMSampleGroupCriteriaDTO {

    private String sampleGroupName;
    private Collection<KMSampleDTO> kmSampleDTOCollection;
    private Color color;

    /**
     * Get the color.
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color.
     * @param color the color to set.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the sample group name.
     * @return the sample group name
     */
    public String getSampleGroupName() {
        return sampleGroupName;
    }

    /**
     * Set the sample group name.
     * @param sampleGroupName the sample group name to set.
     */
    public void setSampleGroupName(String sampleGroupName) {
        this.sampleGroupName = sampleGroupName;
    }

    /**
     * Get the KM sample DTO collection.
     * @return the KM sample DTO collection
     */
    public Collection<KMSampleDTO> getKmSampleDTOCollection() {
        return kmSampleDTOCollection;
    }

    /**
     * Set the KM sample DTO collection.
     * @param kmSampleDTOCollection the KM sample DTO collection to set
     */
    public void setKmSampleDTOCollection(Collection<KMSampleDTO> kmSampleDTOCollection) {
        this.kmSampleDTOCollection = kmSampleDTOCollection;
    }
}
