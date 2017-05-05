package com.evnica.endomondo.matching;

import org.joda.time.DateTime;

/**
 * Project: Endomondo
 * Class: SegmentPairedWithPoint
 * Version: 0.1
 * Created on 4/17/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class SegmentPairedWithPoint
{
    int segmentId;
    int pointId;
    int source;
    int target;
    DateTime dt;
    double distance_offset;
    double duration_offset;
    double cost = 0;
    Boolean activityInDigitizationDirection = null;
    boolean complete = false;


    @Override
    public String toString() {
        return pointId + "\t" + segmentId + "\t" + dt + "\t " + source + "\t" + target +
                "\t" + distance_offset + "\t" + duration_offset + "\t" + activityInDigitizationDirection + "\t" + complete;

    }
}
