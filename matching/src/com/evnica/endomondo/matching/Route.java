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
public class Route
{
    List<SegmentPairedWithPoint> roadSegments;
    Boolean loop = false,
            deadEnd = false;
    boolean dubious = false;
}
