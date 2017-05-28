package com.evnica.endomondo.matching;

import java.util.List;

/**
 * Project: Endomondo
 * Class: Route
 * Version: 0.1
 * Created on 5/2/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class Route
{
    List<SegmentPairedWithPoint> roadSegments;
    int id;
    int deadEnds = -1, loops = -1;
    boolean dubious = false;
    boolean firstDistinct = false, lastDistinct = false;

    @Override
    public String toString()
    {
        int segmentCount = roadSegments != null ? roadSegments.size() : 0;
        return id + ", " + segmentCount + " segments, loops " + loops + ", deadEnds " + deadEnds + ", dubious " +
                            dubious + ", distinct: first " + firstDistinct + ", last " + lastDistinct;
    }
}
