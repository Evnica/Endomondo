package com.evnica.endomondo.dbhandling.main;


/**
 * Project: Endomondo
 * Class: ConverterMain
 * Version: 0.1
 * Created on 4/2/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class ConverterMain
{
    public static void main(String[] args) {

        Converter.readParameters();

        if(Converter.athleteDirectory != null)
        {
            Thread athleteProcessor = new Thread(new AthleteConverter());
            athleteProcessor.setName("AthleteConverter");
            athleteProcessor.start();
        }
        if (Converter.workoutDirectory != null)
        {
            Thread workoutProcessor = new Thread(new WorkoutConverter());
            workoutProcessor.setName("WorkoutConverter");
            workoutProcessor.start();
        }
    }
}
