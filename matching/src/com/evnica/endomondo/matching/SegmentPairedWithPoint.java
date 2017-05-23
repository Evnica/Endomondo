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

    public int getPointId() {
        return pointId;
    }

    SegmentPairedWithPoint() {
    }

    SegmentPairedWithPoint(int segmentId) {
        this.segmentId = segmentId;
    }

    public SegmentPairedWithPoint(SegmentPairedWithPoint s) {
        this.segmentId = s.segmentId;
        this.pointId = s.pointId;
        this.source = s.source;
        this.target = s.target;
        this.dt = s.dt;
        this.distance_offset = s.distance_offset;
        this.duration_offset = s.duration_offset;
        this.cost = s.cost;
        this.activityInDigitizationDirection = s.activityInDigitizationDirection;
        this.complete = s.complete;
    }

    @Override
    public String toString() {
        return pointId + "\t" + segmentId + "\t" + dt + "\t " + source + "\t" + target +
                "\t" + distance_offset + "\t" + duration_offset + "\t" + activityInDigitizationDirection + "\t" + complete;

    }
}
