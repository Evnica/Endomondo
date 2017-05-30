package com.evnica.endomondo.matching;

/**
 * Project: Endomondo
 * Class: TimeInterval
 * Version: 0.1
 * Created on 5/28/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public enum TimeInterval
{
    ZERO("00:00-04:59"),
    ONE("05:00-09:59"),
    TWO("10:00-14:59"),
    THREE("15:00-19:59"),
    FOUR("20:00-23:59");

    String description;

    private TimeInterval(String description)
    {
        this.description = description;
    }


    @Override
    public String toString() {
        return description;
    }

    static int toInt(TimeInterval timeInterval)
    {
        int result = -1;
        switch (timeInterval)
        {
            case ZERO:
                result = 0;
                break;
            case ONE:
                result = 1;
                break;
            case TWO:
                result = 2;
                break;
            case THREE:
                result = 3;
                break;
            case FOUR:
                result = 4;
                break;
        }
        return result;
    }

}
