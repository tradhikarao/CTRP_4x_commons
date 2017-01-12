package com.fiveamsolutions.nci.commons.kmplot.util;

/**
 * @author caIntegrator Team
*/

public enum ImageTypes {

    /**
     * JPEG image.
     */
    JPEG("jpeg"),

    /**
     * PNG image.
     */
    PNG("png");

    private final String value;

    /**
     * Create a new  ImageTypes.
     * @param s the value of the ImageTypes
     */
    ImageTypes(String s) {
         this.value = s;
    }

    /**
     * Get the value for this ImageTypes.
     * @return the value for this ImageTypes
     */
    public String getValue() {
        return value;
    }
}
