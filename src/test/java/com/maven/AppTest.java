package com.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppTest {
    @Test
    public void testGetMessage() {
        App app = new App();
        assertEquals("Hello Maven", app.getMessage());
    }
    
    @Test
    public void testApp() {
        assertTrue(true);
    }
}