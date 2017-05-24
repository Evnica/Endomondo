package com.evnica.endomondo.matching;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Project: Endomondo
 * Class: MatchingMain
 * Version: 0.1
 * Created on 5/23/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class MatchingMain
{
    public static void main(String[] args)
    {
        Communicator.initialize();
        List<Integer> ids = Communicator.getWrktIds();
        for (int id: ids)
        {
            System.out.println("Starting workout " + id);
            try
            {
                if (Communicator.selectWrktPointsIntoTable(id))
                System.out.println(id +": Points inserted");

                String extent = Communicator.calculateExtent();

                if (Communicator.createNetworkViewForExtent(extent))
                System.out.println(id + ": Extent calculated");

                if (Communicator.fillInWorkoutDetail(id))
                System.out.println(id + ": Wrkt detail filled");

                LinkedList<SegmentPairedWithPoint> segments = Communicator.intersectBuffers();
                System.out.println(id + ": Buffers intersected");

                Route probableRoute = Communicator.restoreTrip(segments);
                System.out.println(id + ": Trip restored");

                try
                {
                    Communicator.insertRouteIntoDb(probableRoute);
                    System.out.println("Route details saved in db");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                try
                {
                    Communicator.insertRouteSegmentsIntoDb(probableRoute, Communicator.currentWorkout.getUserId());
                    System.out.println("Segments inserted");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                Communicator.pointCount = 0;
            }
            catch (Exception e)
            {
                System.out.println(id + " wrkt was faulty: " + e.getMessage());
                e.printStackTrace();
            }
        }
        Communicator.terminate();


    }
}
