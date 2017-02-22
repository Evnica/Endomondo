package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.model.Point;
import com.evnica.endomondo.main.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: GooglePolylineDecoder
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class GooglePolylineDecoder
{
    public static Polyline decode(String encoded) {

        List<Point> points = new ArrayList<>();

        int index = 0;
        int lat = 0,
            lon = 0;

        while (index < encoded.length())
        {
            int b,
                result = 0,
                shift = 0;
            do
            {
                // to ensure proper display of these characters,
                // encoded values are summed with 63 (the ASCII character '?')
                // before converting them into ASCII
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);

            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;

            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);

            int deltaLon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += deltaLon;

            points.add(new Point(((double) lat / 1E5), ((double) lon / 1E5)));
        }

        return new Polyline( points );
    }
}
