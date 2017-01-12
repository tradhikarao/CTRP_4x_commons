package com.fiveamsolutions.nci.commons.kmplot.util.model;

import java.awt.Color;
import java.io.Serializable;

import com.fiveamsolutions.nci.commons.kmplot.util.model.KMPlotPointSeries.SeriesType;

/**
 * @author caIntegrator Team
 */

/**
 * This Class contains two KaplainMeierPlotPointSeries. One for the probability
 * line and a second series of Censor plot points.  This series also allows the
 * user to define certain attributes of how they would like the actual pair to
 * be rendered on the JFreeChart.  Because both series are related by the sample
 * set that was used to generate them, they will overlay each other and appear
 * as one plot.  For this reason it is assumed that the entire set will be
 * the same color.  If no color is set, black will be used.
 *
 */
public class KMPlotPointSeriesSet implements Serializable {
    private static final long serialVersionUID = 1L;
    private KMPlotPointSeries censorPlotPoints = null;
    private KMPlotPointSeries probabilityPlotPoints = null;
    private String legendTitle = "";
    private String name = "";
    @SuppressWarnings("rawtypes")
    private final Comparable hashKey;
    private Color color;
    private Integer groupSize;

    /**
     * Default Constructor.
     */
    public KMPlotPointSeriesSet() {
        hashKey = createHash();
    }

    /**
     * @return Returns the censorPlotPoints.
     */
    public KMPlotPointSeries getCensorPlotPoints() {
        return censorPlotPoints;
    }
    /**
     * @param censorScatterDataPoints The censorPlotPoints to set.
     */
    public void setCensorPlotPoints(KMPlotPointSeries censorScatterDataPoints) {
        this.censorPlotPoints = censorScatterDataPoints;
        this.censorPlotPoints.setKey(hashKey);
        this.censorPlotPoints.setSeriesType(SeriesType.CENSOR);
    }
    /**
     * @return Returns the probabilityPlotPoints.
     */
    public KMPlotPointSeries getProbabilityPlotPoints() {
        return probabilityPlotPoints;
    }
    /**
     * @param xyLineDataPoints The probabilityPlotPoints to set.
     */
    public void setProbabilityPlotPoints(KMPlotPointSeries xyLineDataPoints) {
        this.probabilityPlotPoints = xyLineDataPoints;
        this.probabilityPlotPoints.setKey(hashKey);
        this.probabilityPlotPoints.setSeriesType(SeriesType.PROBABILITY);
    }
    /**
     * @return Returns the color.
     */
    public Color getColor() {
        return color;
    }
    /**
     * @param color The color to set.
     */
    public void setColor(Color color) {
        this.color = color;
    }
    /**
     * @return Returns the legendTitle.
     */
    public String getLegendTitle() {
        return legendTitle;
    }
    /**
     * @param legendTitle The legendTitle to set.
     */
    public void setLegendTitle(String legendTitle) {
        this.legendTitle = legendTitle;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This function is required to createPlot a unique id that will be used
     * to identify it's members later by the Plotting class.  This is necessary
     * because at the survivalLength if this writing, you could not get access to the
     * data set once it was placed into a JFreeChart.
     *
     */
    @SuppressWarnings("rawtypes")
    private Comparable createHash() {
        return Math.random() * System.currentTimeMillis();
    }

    /**
     * A unique id that represents all members of this set.
     * @return Returns the hashKey.
     */
    @SuppressWarnings("rawtypes")
    public Comparable getHashKey() {
        return hashKey;
    }

    /**
     * @return the groupSize
     */
    public Integer getGroupSize() {
        return groupSize;
    }

    /**
     * @param groupSize the groupSize to set
     */
    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

}
