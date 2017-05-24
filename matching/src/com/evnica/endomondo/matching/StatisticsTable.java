package com.evnica.endomondo.matching;

/**
 * Project: Endomondo
 * Class: StatisticsTable
 * Version: 0.1
 * Created on 5/23/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public enum StatisticsTable
{
    DISTANCE ("tract_distances"),
    DURATION ("tract_durations"),
    SPEED ("tract_speeds"),
    AGE ("tract_age_gender");

    private String tableName;

    private StatisticsTable(String tableName)
    {
        this.tableName = tableName;
    }


    @Override
    public String toString() {
       return tableName;
    }
}
