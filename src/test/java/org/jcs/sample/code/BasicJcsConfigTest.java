package org.jcs.sample.code;


import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class BasicJcsConfigTest {


    public static final String TEST_CACHE_REGION = "testCache1";
    public static final int MAX_MEMORY_IDLE_TIME = 5;
    public static final String BASIC_JCS_CONFIG_FILE_NAME = "BasicJcsConfigTest.ccf";

    @Test
    public void should_automatically_evict_objects_from_cache() throws CacheException, InterruptedException {

        // Prepare
        JCS jcs = JCS.getInstance(TEST_CACHE_REGION);

        TestEventHandler eventHandler = new TestEventHandler();
        Map<String, String> elementsPutIntoCache = putElementsIntoCache(jcs, eventHandler, 9);

        // Exercise
        waitForCacheToEvictElements();

        // Verify
        assertElementsWereEvictedFromCache(jcs, elementsPutIntoCache);
    }

    private void assertElementsWereEvictedFromCache(JCS jcs, Map<String, String> elementsPutIntoCache) {
        for (String key : elementsPutIntoCache.keySet())
            assertNull(jcs.get(key));

    }

    @Test
    public void should_fire_the_event_listener_when_evicting_objects_from_cache() throws CacheException, InterruptedException {

        // Prepare
        JCS jcs = JCS.getInstance(TEST_CACHE_REGION);

        TestEventHandler eventHandler = new TestEventHandler();
        putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        waitForCacheToEvictElements();

        // Verify
        assertEventFiredForElementEvictedFromCache(eventHandler);
    }

    @Test
    public void should_spool_elements_from_cache_and_fire_the_appropriate_event() throws CacheException, InterruptedException {

        // Prepare
        JCS jcs = JCS.getInstance(TEST_CACHE_REGION);
        TestEventHandler eventHandler = new TestEventHandler();
        Map<String, String> elementsPutIntoCache = putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        jcs.freeMemoryElements(elementsPutIntoCache.size());
        waitForEventsToBeFired();

        // Verify
        assertElementsSpooledButDiskNotAvailable(eventHandler, elementsPutIntoCache.size());
    }

    private void waitForEventsToBeFired() throws InterruptedException {
        Thread.sleep(2000);
    }

    private void assertElementsSpooledButDiskNotAvailable(TestEventHandler eventHandler, int expectedNoOfEvents) {
        List<IElementEvent> eventsReceived = eventHandler.getEventsReceived();
        assertEquals(expectedNoOfEvents, eventsReceived.size());
        for (IElementEvent event : eventsReceived) {
            assertEquals(IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE, event.getElementEvent());
        }
    }

    private void assertEventFiredForElementEvictedFromCache(TestEventHandler eventHandler) {
        List<IElementEvent> eventsReceived = eventHandler.getEventsReceived();
        assertEquals(1, eventsReceived.size());
        assertEquals(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND, eventsReceived.get(0).getElementEvent());
    }

    private void waitForCacheToEvictElements() throws InterruptedException {
        int maxMemoryIdlePlusOne = (MAX_MEMORY_IDLE_TIME + 1) * 1000;
        Thread.sleep(maxMemoryIdlePlusOne);
    }

    private Map<String, String> putElementsIntoCache(JCS jcs, IElementEventHandler eventHandler, int noOfElement) throws CacheException {
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler(eventHandler);

        Map<String, String> elementsPutInTheCache = new HashMap<String, String>();

        for (int i = 0; i < noOfElement; i++) {
            String key = "key" + i;
            String val = "data" + i;
            jcs.put(key, val, attributes);
            elementsPutInTheCache.put(key, val);
        }

        return elementsPutInTheCache;
    }


}

class TestEventHandler implements IElementEventHandler {

    private List<IElementEvent> eventsReceived = new ArrayList<IElementEvent>();

    @Override
    public void handleElementEvent(IElementEvent event) {
        System.out.println("Received event: " + event.getElementEvent());
        eventsReceived.add(event);
    }

    List<IElementEvent> getEventsReceived() {
        return eventsReceived;
    }
}