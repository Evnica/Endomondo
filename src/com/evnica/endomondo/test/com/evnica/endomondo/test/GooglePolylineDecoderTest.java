package com.evnica.endomondo.test;

import com.evnica.endomondo.main.decode.GooglePolylineDecoder;
import com.evnica.endomondo.main.model.Point;
import com.evnica.endomondo.main.model.Polyline;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Class: GooglePolylineDecoderTest
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class GooglePolylineDecoderTest
{
    private String polyline;
    @Before
    public void setUp() throws Exception
    {
        polyline = "ymxiJew~uCYHe@`@k@FOqASF]qCq@sDuByC_@qAy@cDQa@kBiBi@c@e@Ho@Ns@o@s@][j@Ux@c@t@WlBi@hCuAvI]dBKnAiAnG";
    }

    @Test
    public void testDecode()
    {
        Polyline poly = GooglePolylineDecoder.decode( polyline );
        Point start = new Point( 59.44045, 24.73859 ),
                end = new Point( 59.44617, 24.73746 ),
              third = new Point( 59.44077, 24.73837);
        assertTrue( start.equals( poly.getPoint( 0 ) ) );
        assertTrue( third.equals( poly.getPoint( 2 ) ) );
        assertTrue( end.equals( poly.getPoint( poly.size() - 1 ) ) );

    }

}