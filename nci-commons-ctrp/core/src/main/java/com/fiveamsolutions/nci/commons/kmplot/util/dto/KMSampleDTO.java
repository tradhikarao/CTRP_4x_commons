package com.fiveamsolutions.nci.commons.kmplot.util.dto;

import java.io.Serializable;

/**
 * @author caIntegrator Team
 * This class represents the data associated with a sample.
*/

public class KMSampleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int survivalLength;
    private final boolean censor;

    /**
     * @param t the survival length
     * @param c the censor flag
     */
    public KMSampleDTO(int t, boolean c) {
        this.survivalLength = t;
        this.censor = c;
    }

    /**
     * @return Returns the censor.
     */
    public boolean isCensor() {
        return censor;
    }

    /**
     * @return Returns the survivalLength.
     */
    public int getSurvivalLength() {
        return survivalLength;
    }
}
