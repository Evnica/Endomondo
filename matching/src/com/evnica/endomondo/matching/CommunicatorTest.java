package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.matching.Communicator
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class CommunicatorTest
{
    @Before
    public void setUp() throws Exception
    {
        assertTrue(Communicator.initialize());
    }

    @After
    public void tearDown() throws Exception
    {
        assertTrue(Communicator.terminate());
    }

    @Test
    public void selectWrktPointsIntoTable() throws Exception
    {
        assertTrue(Communicator.selectWrktPointsIntoTable(651272641));
        //String extent = Communicator.calculateExtent();
        //assertTrue(Communicator.createNetworkViewForExtent(extent));
    }

    @Test
    public void calculateExtent()
    {
        System.out.println(Communicator.calculateExtent());
    }

    @Test
    public void createNetworkViewForExtent()
    {
        String extent = Communicator.calculateExtent();
        assertTrue(Communicator.createNetworkViewForExtent(extent));
    }

    @Test
    public void intersectBuffersAndMore() throws Exception
    {
        List<Integer> ids = Communicator.getWrktIds();
        for (int id: ids)
        {
            try {
                assertTrue(Communicator.selectWrktPointsIntoTable(id));
                assertTrue(Communicator.fillInWorkoutDetail(id));
                LinkedList<SegmentPairedWithPoint> segments = Communicator.intersectBuffers();
                Route probableRoute = Communicator.restoreTrip(segments);

                try {
                    Communicator.insertRouteIntoDb(probableRoute);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Communicator.insertRouteSegmentsIntoDb(probableRoute, Communicator.currentWorkout.getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Communicator.pointCount = 0;
            } catch (Exception e) {
                System.out.println(id + " wrkt was faulty: ");
                e.printStackTrace();
            }
        }

        DbConnector.closeConnection();

        System.out.println("Can you believe it?");

    }

    @Test
    public void findLoops()
    {
        LinkedList<SegmentPairedWithPoint> probableList = new LinkedList<>();
        probableList.add(new SegmentPairedWithPoint(0));
        probableList.add(new SegmentPairedWithPoint(1));
        probableList.add(new SegmentPairedWithPoint(2));
        probableList.add(new SegmentPairedWithPoint(3));
        probableList.add(new SegmentPairedWithPoint(4));
        probableList.add(new SegmentPairedWithPoint(5));
        probableList.add(new SegmentPairedWithPoint(2));
        probableList.add(new SegmentPairedWithPoint(7));
        probableList.add(new SegmentPairedWithPoint(8));
        probableList.add(new SegmentPairedWithPoint(9));
        probableList.add(new SegmentPairedWithPoint(10));
        probableList.add(new SegmentPairedWithPoint(11));
        probableList.add(new SegmentPairedWithPoint(12));
        probableList.add(new SegmentPairedWithPoint(13));
        probableList.add(new SegmentPairedWithPoint(8));
        probableList.add(new SegmentPairedWithPoint(15));
        probableList.add(new SegmentPairedWithPoint(16));
        probableList.add(new SegmentPairedWithPoint(17));
        probableList.add(new SegmentPairedWithPoint(18));
        probableList.add(new SegmentPairedWithPoint(19));
        probableList.add(new SegmentPairedWithPoint(20));
        probableList.add(new SegmentPairedWithPoint(16));
        probableList.add(new SegmentPairedWithPoint(17));
        probableList.add(new SegmentPairedWithPoint(23));
        probableList.add(new SegmentPairedWithPoint(24));

        Route r = new Route();

        Communicator.determineLoops(probableList, r);
        System.out.println(r);
        for (SegmentPairedWithPoint s: r.roadSegments)
        {
            System.out.print(s.segmentId + " ");
        }
    }

}