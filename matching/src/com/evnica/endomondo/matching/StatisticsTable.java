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
    DISTANCE ("florida_distances"),//("tract_distances"),
    DURATION ("florida_durations"),//("tract_durations"),
    SPEED ("florida_speeds"),//("tract_speeds"),
    AGE ("florida_age_gender");//("tract_age_gender");

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
