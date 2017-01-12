/*
 * Created on Nov 17, 2004
 */
package com.fiveamsolutions.nci.commons.kmplot.util.model;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;



/**
 * @author caIntegrator Team
 * This class was written to store Kaplan-Meier data points
 * as they are input. It extends the XYSeries from the JFreeChart
 * package.  This resolves an issue with the superclass sorting
 * the inputs by X and Y, messing up the step graph data lines.
 *
 */

@SuppressWarnings({ "unchecked", "serial" })
 public class KMPlotPointSeries extends XYSeries {
    private SeriesType myType;

    /**
     * @param string the series name
     * @param arg the argument
     */
    public KMPlotPointSeries(String string, boolean arg) {
        super(string, arg);
    }

    /**
     * Set the series type.
     * @param type the series type to set.
     */
    public void setSeriesType(SeriesType type) {
        myType = type;
    }

    /**
     * Get the series type.
     * @return the series type
     */
    public SeriesType getType() {
        return myType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(XYDataItem xyDataPair) {
        data.add(xyDataPair);
    }

    /**
     * Add a point to the series.
     * @param xyDataPair the point
     * @param index the index of the point
     */
    public void add(XYDataItem xyDataPair, int index) {
        data.add(index, xyDataPair);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * SeriesType enum.
     */
    public enum SeriesType {

        /**
         * Probability series.
         */
        PROBABILITY,

        /**
         * Censor series.
         */
        CENSOR
    }
}
