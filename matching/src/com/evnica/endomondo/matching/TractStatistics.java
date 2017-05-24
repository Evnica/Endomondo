package com.evnica.endomondo.matching;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 5/23/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class TractStatistics
{
    String id;
    Statistics  distanceStatistics = Statistics.getEmpty(),
                durationStatistics = Statistics.getEmpty(),
                speedStatistics = Statistics.getEmpty(),
                ageStatistics = Statistics.getEmpty();
    double      percentageOfMale = -1;
}
