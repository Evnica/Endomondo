package com.evnica.endomondo.main.model;

/**
 * Class: WorkoutGeometryType
 * Version: 0.1
 * Created on 05.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public enum WorkoutGeometryType
{
    POINTS("Points", 0),
    EMPTY_POINTS("Empty points", 1),
    LAPS("Laps", 2),
    EMPTY_LAPS("Laps without polylines", 3),
    LAPS_POINTS("Laps and points", 4),
    EMPTY_LAPS_POINTS("Laps without polylines, points with coordinates", 5),
    EMPTY_LAPS_EMPTY_POINTS("Both laps and points empty", 6),
    NONE("None", 7);

    private String name;
    private int code;

    WorkoutGeometryType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public int getCode()
    {
        return code;
    }

}
