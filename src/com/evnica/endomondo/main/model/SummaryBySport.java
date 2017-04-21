package com.evnica.endomondo.main.model;

/**
 * Class: SummaryBySport
 * Version: 0.1
 * Created on 06.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class SummaryBySport
{
    public int sport;
    public int count;
    public double totalDistance, totalDuration;

    @Override
    public String toString()
    {
        //13718401	3	117	1691040	6295.89424697876
        return sport + "\t" + count + "\t" + String.format("%.2f", totalDistance)
                + "\t" + String.format("%.2f", totalDuration);
    }
}
