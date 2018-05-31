package com.xuhuanli.piccompress;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = new MainActivity();
    }

    @Test
    public void mathHalf() {
        assertEquals(32,mainActivity.mathHalf(3880,5184,200,200,1));
    }
}