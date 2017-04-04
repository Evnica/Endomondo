package com.evnica.endomondo.dbhandling.main;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 4/2/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class ConverterMain
{
    public static void main(String[] args) {

        Converter.readParameters();
        if(Converter.processAthletes)
        {
            int[] result = new AthleteConverter().process();
            for (int i: result) System.out.print(i + "; ");
        }
        else
        {
            int[] result = new WorkoutConverter().process();
            for (int i: result) System.out.print(i + "; ");
        }
    }
}
