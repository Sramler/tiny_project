package com.tiny.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TinyWebAllInOneApplicationTests {

    @Test
    void applicationClassLoadsWithoutSpringContext() {
        TinyWebAllInOneApplication application = new TinyWebAllInOneApplication();
        assertNotNull(application);
    }
}
