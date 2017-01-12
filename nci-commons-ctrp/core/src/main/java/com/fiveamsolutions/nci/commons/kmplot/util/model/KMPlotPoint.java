/*
 * Created on Oct 12, 2004
 *
 */
package com.fiveamsolutions.nci.commons.kmplot.util.model;

import org.jfree.data.xy.XYDataItem;

/**
 * @author caIntegrator Team
 */

/**
 * This class extends the JFreeChart class XYDataItem and
 * adds a third parameter for census. If census == true,
 * draw a "+" at that point.
 */

@SuppressWarnings("serial")
public class KMPlotPoint extends XYDataItem {

    private final boolean checked;

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @param b checked
     */
    public KMPlotPoint(Number x, Number y, boolean b) {
        super(x, y);
        this.checked = b;
    }

    /**
     * @return Returns the isCensus
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ("( " + getX() + ", " + getY() + ")  Census:" + checked);
    }
}
