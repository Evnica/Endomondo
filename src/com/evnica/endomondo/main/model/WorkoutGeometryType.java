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
    VPOINTS_VLAPS("Valid points and valid laps", 0),
    VPOINTS_ALAPS("Valid points, absent laps", 1),
    VPOINTS_ILAPS("Valid points, invalid laps", 2),
    IPOINTS_VLAPS("Invalid points, valid laps", 3),
    IPOINTS_ALAPS("Invalid points, absent laps", 4),
    IPOINTS_ILAPS("Invalid points, invalid laps", 5),
    APOINTS_VLAPS("Absent points, valid laps", 6),
    APOINTS_ALAPS("Absent points, absent laps", 7),
    APOINTS_ILAPS("Absent points, invalid laps", 8);

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
