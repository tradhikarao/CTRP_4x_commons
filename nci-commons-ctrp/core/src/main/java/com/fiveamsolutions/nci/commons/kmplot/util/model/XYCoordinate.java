package com.fiveamsolutions.nci.commons.kmplot.util.model;

/**
 * A data point.
 */
public class XYCoordinate {

    private boolean checked = false;
    private Number x;
    private Number y;

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public XYCoordinate(Number x, Number y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @param b checked
     */
    public XYCoordinate(Number x, Number y, boolean b) {
        this(x, y);
        this.checked = b;
    }

    /**
     * Get the x coordinate.
     * @return the x coordinate
     */
    public Number getX() {
        return x;
    }

    /**
     * Get the y coordinate.
     * @return the y coordinate
     */
    public Number getY() {
        return y;
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
