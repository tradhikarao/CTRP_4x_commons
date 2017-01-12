package com.fiveamsolutions.nci.commons.kmplot.util.model;

import java.awt.Color;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Ram Bhattaru
 * Date: Sep 12, 2007
 * Time: 11:33:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class GroupCoordinates {
    private final Collection<XYCoordinate> dataPoints;
    private final String groupName;
    private final Color color;
    private final Integer groupSize;

    /**
     * @param dataPoints the data points.
     * @param groupName the group name.
     * @param color the color.
     * @param groupSize the group size.
     */
    public GroupCoordinates(Collection<XYCoordinate> dataPoints, String groupName, Color color, Integer groupSize) {
        this.dataPoints = dataPoints;
        this.groupName = groupName;
        this.color = color;
        this.groupSize = groupSize;
    }

    /**
     * Get the data points.
     * @return the data points.
     */
    public Collection<XYCoordinate> getDataPoints() {
        return dataPoints;
    }

    /**
     * Get the group name.
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Get the color.
     * @return the color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the group size.
     * @return the group size.
     */
    public Integer getGroupSize() {
        return groupSize;
    }
}
