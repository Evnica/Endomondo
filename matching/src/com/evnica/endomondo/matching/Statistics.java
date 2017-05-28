package com.evnica.endomondo.matching;

/**
 * Project: Endomondo
 * Class: Statistics
 * Version: 0.1
 * Created on 5/23/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class Statistics
{
    Double  min,
            max,
            median,
            mean,
            trimmedMean,
            stDev,
            lowerQuartile,
            upperQuartile,
            lastNonOutlier;
    Integer totalCount,
            outlierCount;
    boolean sample30orMore = false;

    static Statistics getEmpty()
    {
        Statistics s = new Statistics();
        s.min = -1.0;
        s.max = -1.0;
        s.median = -1.0;
        s.mean = -1.0;
        s.trimmedMean = -1.0;
        s.stDev = -1.0;
        s.lowerQuartile = -1.0;
        s.upperQuartile = -1.0;
        s.lastNonOutlier = -1.0;
        s.totalCount = 0;
        s.outlierCount = -1;

        return s;
    }

    @Override
    public String toString() {
        return min + "\t" + max + "\t" + median + "\t" + mean + "\t" + trimmedMean + "\t" + stDev + "\t" + lowerQuartile
                + "\t" + upperQuartile + "\t" + totalCount + "\t" + outlierCount + "\t" + sample30orMore;
    }
}
