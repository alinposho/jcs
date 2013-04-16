package org.jcs.sample.code;


import junit.framework.Assert;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class BasicJcsConfigTest {


    public static final String TEST_CACHE_REGION = "testCache1";
    private static final int MAX_MEMORY_IDLE_TIME = 5;

    @Test
    public void testAddListener() throws CacheException {

        JCS jcs = JCS.getInstance(TEST_CACHE_REGION);
        putElementIntoCache(jcs);

        jcs.freeMemoryElements(1);
    }

    private void putElementIntoCache(JCS jcs) throws CacheException {
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler(createEventHandlerForSpolledDiskNotAvailable());
        jcs.put("key", "data", attributes);
    }

    private IElementEventHandler createEventHandlerForSpolledDiskNotAvailable() {
        return new IElementEventHandler() {
            @Override
            public void handleElementEvent(IElementEvent iElementEvent) {
                assertEquals(IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE, iElementEvent.getElementEvent());
                System.out.println("Event received " + iElementEvent.getElementEvent());
            }
        };
    }

    @Test
    public void should_fire_the_event_listener_when_evicting_oject_from_cache() throws CacheException, InterruptedException {

        JCS jcs = JCS.getInstance(TEST_CACHE_REGION);
        putElementIntoCache(jcs);

        Thread.sleep((MAX_MEMORY_IDLE_TIME + 1) * 1000);

    }

}