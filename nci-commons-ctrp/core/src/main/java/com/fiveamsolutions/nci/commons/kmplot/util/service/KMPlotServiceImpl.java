package com.fiveamsolutions.nci.commons.kmplot.util.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.fiveamsolutions.nci.commons.kmplot.util.DefaultKMAlgorithmImpl;
import com.fiveamsolutions.nci.commons.kmplot.util.ImageTypes;
import com.fiveamsolutions.nci.commons.kmplot.util.JFreeChartIKMPlottermpl;
import com.fiveamsolutions.nci.commons.kmplot.util.KMAlgorithm;
import com.fiveamsolutions.nci.commons.kmplot.util.KMException;
import com.fiveamsolutions.nci.commons.kmplot.util.KMPlotter;
import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMCriteriaDTO;
import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMSampleDTO;
import com.fiveamsolutions.nci.commons.kmplot.util.dto.KMSampleGroupCriteriaDTO;
import com.fiveamsolutions.nci.commons.kmplot.util.model.GroupCoordinates;
import com.fiveamsolutions.nci.commons.kmplot.util.model.XYCoordinate;

/**
 @author caIntegrator Team
 */

public class KMPlotServiceImpl implements KMPlotService {
    private static final Logger LOGGER = Logger.getLogger(KMPlotServiceImpl.class);
    private KMPlotter plotter = new JFreeChartIKMPlottermpl();
    private KMAlgorithm kmAlgorithm = new DefaultKMAlgorithmImpl();

    /**
     * The default color.
     */
    public static final Color DEFAULT_COLOR = Color.CYAN;

    /**
     * Get the KMPlotter.
     * @return the KMPlotter
     */
    public KMPlotter getPlotter() {
        return plotter;
    }

    /**
     * Set the KMPlotter.
     * @param plotter the KMPlotter to set.
     */
    public void setPlotter(KMPlotter plotter) {
        this.plotter = plotter;
    }

    /**
     * Get the KMAlgorithm.
     * @return the KMAlgorithm
     */
    public KMAlgorithm getKmAlgorithm() {
        return kmAlgorithm;
    }

    /**
     * Set the KMAlgorithm.
     * @param kmAlgorithm the KMAlgorithm to set.
     */
    public void setKmAlgorithm(KMAlgorithm kmAlgorithm) {
        this.kmAlgorithm = kmAlgorithm;
    }

    /**
     * {@inheritDoc}
     */
    public Double computeLogRankPValueBetween(KMSampleGroupCriteriaDTO group1, KMSampleGroupCriteriaDTO group2) {
        Collection<KMSampleDTO> sampleGroup1 = group1.getKmSampleDTOCollection();
        Collection<KMSampleDTO> sampleGroup2 = group2.getKmSampleDTOCollection();
        return kmAlgorithm.getLogRankPValue(sampleGroup1, sampleGroup2);
    }

    /**
     * {@inheritDoc}
     */
    public void writePlotToOutputStream(OutputStream out, KMCriteriaDTO kmCrit, ImageTypes imgType)
    throws KMException {
        java.awt.image.BufferedImage image = buildImage(kmCrit);
        plotter.writeBufferedImage(out, image, imgType);
    }

    /**
     * {@inheritDoc}
     */
    public void createPlotAsImageFile(File outFile, KMCriteriaDTO kmCrit, ImageTypes imgType)
    throws KMException {
        assert kmCrit != null;
        ImageTypes actualImageType = imgType;
        if (actualImageType == null) {
            actualImageType = ImageTypes.PNG;  // default to png format
        }
        try {
            FileOutputStream fileOutStream = new FileOutputStream(outFile);
            writePlotToOutputStream(fileOutStream, kmCrit, actualImageType);
            fileOutStream.close();
        } catch (IOException e) {
            LOGGER.debug(e);
            throw new KMException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage createBufferedImage(KMCriteriaDTO kmCrit) {
        return buildImage(kmCrit);
    }

    private java.awt.image.BufferedImage  buildImage(KMCriteriaDTO kmCrit) {
        return plotter.createImage(getGroupsToBePlotted(kmCrit), kmCrit.getPlotTitle(),
                kmCrit.getDurationAxisLabel(), kmCrit.getProbablityAxisLabel());
    }

    private Collection<GroupCoordinates> getGroupsToBePlotted(KMCriteriaDTO kmCrit) {
        Collection<KMSampleGroupCriteriaDTO> sampleGroups = kmCrit.getSampleGroupCriteriaDTOCollection();
        int count = 0;
        Collection<GroupCoordinates> groupsToBePlotted = new ArrayList<GroupCoordinates>();
        for (Iterator<KMSampleGroupCriteriaDTO> iterator = sampleGroups.iterator(); iterator.hasNext(); ++count) {
            KMSampleGroupCriteriaDTO groupCrit =  iterator.next();
            assert (groupCrit != null);
            String groupName = (groupCrit.getSampleGroupName() != null)
                ? groupCrit.getSampleGroupName() : "GROUP " + count;
            Color color = (groupCrit.getColor() != null) ? groupCrit.getColor() : DEFAULT_COLOR;
            Collection<KMSampleDTO>  samples = groupCrit.getKmSampleDTOCollection();
            Collection<XYCoordinate> dataPoints = kmAlgorithm.getPlottingCoordinates(samples);
            GroupCoordinates groupToBePlotted = new GroupCoordinates(dataPoints, groupName, color, samples.size());
            groupsToBePlotted.add(groupToBePlotted);
        }
        return groupsToBePlotted;
    }

    /**
     * {@inheritDoc}
     */
    public Object getChart(KMCriteriaDTO kmCrit) {
        return plotter.createImageOfKnownType(getGroupsToBePlotted(kmCrit), kmCrit.getPlotTitle(),
                kmCrit.getDurationAxisLabel(), kmCrit.getProbablityAxisLabel());
    }
}
