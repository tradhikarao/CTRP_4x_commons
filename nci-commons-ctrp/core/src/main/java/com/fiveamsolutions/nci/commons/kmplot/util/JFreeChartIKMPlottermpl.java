package com.fiveamsolutions.nci.commons.kmplot.util;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeriesCollection;

import com.fiveamsolutions.nci.commons.kmplot.util.model.GroupCoordinates;
import com.fiveamsolutions.nci.commons.kmplot.util.model.KMPlotPoint;
import com.fiveamsolutions.nci.commons.kmplot.util.model.KMPlotPointSeries;
import com.fiveamsolutions.nci.commons.kmplot.util.model.KMPlotPointSeriesSet;
import com.fiveamsolutions.nci.commons.kmplot.util.model.XYCoordinate;
import com.fiveamsolutions.nci.commons.kmplot.util.service.KMPlotServiceImpl;

/**
* @author caIntegrator Team
*/
@SuppressWarnings({"PMD.TooManyMethods", "deprecation" })
public class JFreeChartIKMPlottermpl implements KMPlotter {

    private static final Logger LOGGER = Logger.getLogger(JFreeChartIKMPlottermpl.class);
    private static final int DEFAULT_ENTITY_STATUS = 6;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 500;
    private static final int RECTANGLE_SIZE = 10;

    /**
     * {@inheritDoc}
     */
    public void writeBufferedImage(OutputStream out, BufferedImage image, ImageTypes imgType)
            throws KMException {
        ImageTypes actualImageType = imgType;
        if (actualImageType == null) {
            actualImageType = ImageTypes.PNG;  // default
        }

        try {
            if (actualImageType == ImageTypes.PNG) {
                ChartUtilities.writeBufferedImageAsPNG(out, image);
            } else if (actualImageType == ImageTypes.JPEG) {
                ChartUtilities.writeBufferedImageAsJPEG(out, image);
            } else {
                LOGGER.debug("UnSupported File Format: " + actualImageType.getValue());
                throw new KMException("UnSupported File Format: " + actualImageType.getValue());
            }
        } catch (IOException ioe) {
            LOGGER.debug(ioe);
            throw new KMException(ioe);
        }

    }

    /**
     * write the buffered image as a PNG.
     * @param out the output stream
     * @param image the image
     * @throws IOException on error
     */
    public void writeBufferedImageAsPNG(OutputStream out, BufferedImage image) throws IOException {
        ChartUtilities.writeBufferedImageAsPNG(out, image);
    }

    /**
     * write the buffered image as a JPEG.
     * @param out the output stream
     * @param image the image
     * @throws IOException on error
     */
    public void writeBufferedImageAsJPEG(OutputStream out, BufferedImage image) throws IOException {
        ChartUtilities.writeBufferedImageAsJPEG(out, image);
    }

    /**
     * Create a KM Plot.
     * @param groupsToBePlotted the groups to be plotted.
     * @param title the title of the plot.
     * @param xAxisLabel the x axis label.
     * @param yAxisLabel the y axis label.
     * @return the LM plot.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public JFreeChart createKMPlot(Collection<GroupCoordinates> groupsToBePlotted, String title, String xAxisLabel,
                                    String yAxisLabel) {
        Collection<KMPlotPointSeriesSet> kmPlotSets =
                                    convertToKaplanMeierPlotPointSeriesSet(groupsToBePlotted);

        XYSeriesCollection finalDataCollection =  new XYSeriesCollection();
        /*  Repackage all the datasets to go into the XYSeriesCollection */
        for (KMPlotPointSeriesSet dataSet : kmPlotSets) {
            finalDataCollection.addSeries(dataSet.getCensorPlotPoints());
            finalDataCollection.addSeries(dataSet.getProbabilityPlotPoints());
        }

        JFreeChart chart = ChartFactory.createXYLineChart("", xAxisLabel, yAxisLabel,
                                                finalDataCollection, PlotOrientation.VERTICAL,
                                                true, //legend
                                                true, //tooltips
                                                false); //urls
        XYPlot plot = (XYPlot) chart.getPlot();
        /*
         * Ideally the actual Renderer settings should have been created
         * at the survivalLength of iterating KaplanMeierPlotPointSeriesSets, adding them to the actual
         * Data Set that is going to be going into the Chart plotter.  But you have no idea how
         * they are going to be sitting in the Plot dataset so there is no guarantee that setting the
         * renderer based on a supposed index will actually work. In fact
         */

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < finalDataCollection.getSeriesCount(); i++) {
            KMPlotPointSeries kmSeries = (KMPlotPointSeries) finalDataCollection.getSeries(i);

            if (kmSeries.getType() == KMPlotPointSeries.SeriesType.CENSOR) {
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesVisible(i, true);
            } else if (kmSeries.getType() == KMPlotPointSeries.SeriesType.PROBABILITY) {
                renderer.setSeriesLinesVisible(i, true);
                renderer.setSeriesShapesVisible(i, false);

            } else {
                //don't show this set as it is not a known type
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesVisible(i, false);
            }
            renderer.setSeriesPaint(i, getKMSetColor(kmPlotSets, kmSeries.getKey()), true);
        }

        renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
        renderer.setDefaultEntityRadius(DEFAULT_ENTITY_STATUS);
        plot.setRenderer(renderer);

        /* change the auto tick unit selection to integer units only... */
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        /* OPTIONAL CUSTOMISATION COMPLETED. */
        rangeAxis.setAutoRange(true);
        rangeAxis.setRange(0.0, 1.0);

        /* set Title and Legend */
        chart.setTitle(title);
        createLegend(chart, kmPlotSets);

        return chart;
    }

     /**
      * {@inheritDoc}
      */
     public BufferedImage createImage(Collection<GroupCoordinates> groupsToBePlotted, String title,
             String xAxisLabel, String yAxisLabel) {
        return createKMPlot(groupsToBePlotted, title, xAxisLabel, yAxisLabel)
            .createBufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     }

    /**
     * {@inheritDoc}
     */
    public Object createImageOfKnownType(Collection<GroupCoordinates> groupsToBePlotted, String title,
            String xAxisLabel, String yAxisLabel) {
        return createKMPlot(groupsToBePlotted, title, xAxisLabel, yAxisLabel);
    }

    private Collection<KMPlotPointSeriesSet>
        convertToKaplanMeierPlotPointSeriesSet(Collection<GroupCoordinates> groupsToBePlotted) {
        Collection<KMPlotPointSeriesSet> plotPointSeriesSetCollection = new ArrayList<KMPlotPointSeriesSet>();

        for (Iterator<GroupCoordinates> iterator = groupsToBePlotted.iterator(); iterator.hasNext();) {
            GroupCoordinates groupToPlot = iterator.next();
            Collection<XYCoordinate> points = groupToPlot.getDataPoints();
            KMPlotPoint[] dataPoints = new KMPlotPoint[points.size()];
            int i = 0;
            for (Iterator<XYCoordinate> iterator1 = points.iterator(); iterator1.hasNext();) {
                XYCoordinate xyPoint = iterator1.next();
                KMPlotPoint kmPoint = new KMPlotPoint(xyPoint.getX(), xyPoint.getY(), xyPoint.isChecked());
                dataPoints[i++] = kmPoint;
            }
            KMPlotPointSeriesSet groupPointSeriesSet =
                prepareKaplanMeierPlotPointSeriesSet(dataPoints, groupToPlot.getGroupName(), groupToPlot.getColor());
            groupPointSeriesSet.setGroupSize(groupToPlot.getGroupSize());
            plotPointSeriesSetCollection.add(groupPointSeriesSet);
        }
        return plotPointSeriesSetCollection;
    }

    /**
     * Create a legend for a plot.
     * @param kmPlot the plot
     * @param plotPointSeriesSetCollection the plot point series set collection.
     */
    public static void createLegend(JFreeChart kmPlot, Collection<KMPlotPointSeriesSet> plotPointSeriesSetCollection) {
        LegendTitle legend = kmPlot.getLegend();
        LegendItemSource[] sources = new LegendItemSource[1];
        KMLegendItemSource legendSrc = new KMLegendItemSource();
        LegendItem item;
        for (Iterator<KMPlotPointSeriesSet> iterator = plotPointSeriesSetCollection.iterator(); iterator.hasNext();) {
            KMPlotPointSeriesSet kmPlotPointSeriesSet = iterator.next();
            Color color = kmPlotPointSeriesSet.getColor();
            String title = kmPlotPointSeriesSet.getLegendTitle() + " (" + kmPlotPointSeriesSet.getGroupSize() + ")";
            item = new LegendItem(title, null, null, null,
                    new Rectangle2D.Double(2, 2, RECTANGLE_SIZE, RECTANGLE_SIZE), color);
            legendSrc.addLegendItem(item);
        }
        sources[0] = legendSrc;
        legend.setSources(sources);
     }

    @SuppressWarnings("rawtypes")
    private static Color getKMSetColor(Collection<KMPlotPointSeriesSet> kmPlotSets, Comparable setKey) {
       for (KMPlotPointSeriesSet seriesSet : kmPlotSets) {
           if (seriesSet.getHashKey() == setKey) {
               return seriesSet.getColor();
           }
       }
       return KMPlotServiceImpl.DEFAULT_COLOR;
    }

    /**
     * Legend item source for a KM plot.
     */
    public static class KMLegendItemSource implements LegendItemSource {

        private final LegendItemCollection items = new LegendItemCollection();

        /**
         * {@inheritDoc}
         */
        public void addLegendItem(LegendItem item) {
            items.add(item);
        }

        /**
         * {@inheritDoc}
         */
        public LegendItemCollection getLegendItems() {
           return items;
        }
    }

    /***************************************************************************
     * Creates two data series. One of all data points used to createPlot the step
     * graph, groupName. The second contains the census data that will be
     * overlaid onto the previous step graph to complete the KM Graph,
     * censusSeries. It takes the KaplanMeierPlotPoints and breaks them into two
     * sets of XY points based on whether the data point is checked or not. A
     * checked datapoint shows that it is actually a censor point and should be
     * placed in the scatter plot.
     *
     * @param dataPoints
     * @param groupName
     * @return populated KMPlotPointSeriesSet
     */
    KMPlotPointSeriesSet prepareKaplanMeierPlotPointSeriesSet(KMPlotPoint[] dataPoints,
            String groupName, Color color) {

        // Create the DataPoint Series
        KMPlotPointSeries dataSeries = new KMPlotPointSeries(groupName, true);
        KMPlotPointSeries censusSeries = new KMPlotPointSeries(groupName + "Censor Points ", true);
        LOGGER.debug(groupName);

        for (int i = 0; i < dataPoints.length; i++) {
            LOGGER.debug(dataPoints[i]);
            dataSeries.add(dataPoints[i], i);
            if (dataPoints[i].isChecked()) {
               censusSeries.add(dataPoints[i]);
            }
        }
        dataSeries.setDescription(groupName);
        censusSeries.setDescription(groupName);

        KMPlotPointSeriesSet kmPointSet = new KMPlotPointSeriesSet();
        kmPointSet.setColor(color);
        kmPointSet.setName(groupName);
        kmPointSet.setCensorPlotPoints(censusSeries);
        kmPointSet.setProbabilityPlotPoints(dataSeries);
        kmPointSet.setLegendTitle(groupName);
        return kmPointSet;
    }
}
