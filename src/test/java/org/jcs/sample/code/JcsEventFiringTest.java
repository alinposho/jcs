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

import static org.junit.Assert.*;

public class JcsEventFiringTest {

    private static final String ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND = "ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND";
    private static final String ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE = "ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE";
    private static final String ELEMENT_EVENT_SPOOLED_NOT_ALLOWED = "ELEMENT_EVENT_SPOOLED_NOT_ALLOWED";
    private static final String ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST = "ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST";
    private static final String ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND = "ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND";
    private static final String ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST = "ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST";

    private static final int MAX_MEMORY_IDLE_TIME = 5;
    public static final int SECOND = 1000;
    private static final int ELEMENT_MAX_IDLE = 4;

    // Move to a different test since it doesn't test events
//    @Test
//    public void should_evict_objects_from_cache_in_the_background() throws CacheException, InterruptedException {
//        // Prepare
//        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND);
//        TestEventHandler eventHandler = new TestEventHandler();
//        Map<String, String> elementsPutIntoCache = putElementsIntoCache(jcs, eventHandler, 9);
//
//        // Exercise
//        waitForCacheToEvictElements();
//
//        // Verify
//        assertElementsWereEvictedFromCache(jcs, elementsPutIntoCache);
//    }
//
//    private void assertElementsWereEvictedFromCache(JCS jcs, Map<String, String> elementsPutIntoCache) {
//        for (String key : elementsPutIntoCache.keySet())
//            assertNull(jcs.get(key));
//    }

    @Test
    public void should_fire_ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND);
        TestEventHandler eventHandler = new TestEventHandler();
        putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        waitForCacheToEvictElements();

        // Verify
        assertEventFired(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND, eventHandler);
    }

    /**
     * This test illustrates that the JCS cache doesn't fire the ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST event.
     */
    @Test
    public void should_fire_ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST);
        TestEventHandler eventHandler = new TestEventHandler();
        Map<String, String> elementsPutInCache = putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        accessCacheUntilObjectsGetEvicted(elementsPutInCache, jcs);

        assertEventNotFired(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND, eventHandler);
        waitForEventsToBeFired();

        assertEventFired(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST, eventHandler);
    }

    @Test
    public void should_fire_ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND);
        TestEventHandler eventHandler = new TestEventHandler();
        putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        waitForCacheToEvictElements();

        // Verify
        assertEventFired(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND, eventHandler);
    }

    @Test
    public void should_fire_ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST);
        TestEventHandler eventHandler = new TestEventHandler();
        putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        waitForCacheToEvictElements();

        // Verify
        assertEventFired(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST, eventHandler);
    }

    @Test
    public void should_fire_ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE() throws CacheException, InterruptedException {
        fail("not implemented");
    }

    @Test
    public void should_fire_ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE);
        TestEventHandler eventHandler = new TestEventHandler();
        Map<String, String> elementsPutIntoCache = putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        makeCacheFireSpoolToDiskEvent(jcs, elementsPutIntoCache);

        // Verify
        assertEventFired(IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE, eventHandler);
    }

    @Test
    public void should_fire_ELEMENT_EVENT_SPOOLED_NOT_ALLOWED() throws CacheException, InterruptedException {
        // Prepare
        JCS jcs = loadJcsForRegion(ELEMENT_EVENT_SPOOLED_NOT_ALLOWED);
        TestEventHandler eventHandler = new TestEventHandler();
        Map<String, String> elementsPutIntoCache = putElementsIntoCache(jcs, eventHandler, 1);

        // Exercise
        makeCacheFireSpoolToDiskEvent(jcs, elementsPutIntoCache);

        // Verify
        assertEventFired(IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED, eventHandler);
    }

    private void accessCacheUntilObjectsGetEvicted(Map<String, String> elementsPutInCache, JCS cache) throws InterruptedException {
        boolean elementsEvicted = false;
        do {
            elementsEvicted = true;
            for (String key : elementsPutInCache.keySet()) {
                if (cache.get(key) != null) {
                    elementsEvicted = false;
                    Thread.sleep(1 * SECOND);
                    break;
                }
            }
        } while (!elementsEvicted);
    }

    private void assertEventFired(int eventType, TestEventHandler eventHandler) {
        List<IElementEvent> eventsReceived = eventHandler.getEventsReceived();
        assertEquals(1, eventsReceived.size());
        for (IElementEvent event : eventsReceived) {
            assertEquals(eventType, event.getElementEvent());
        }
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

    private void assertEventNotFired(int eventType, TestEventHandler eventHandler) {
        List<IElementEvent> eventsReceived = eventHandler.getEventsReceived();
        for (IElementEvent event : eventsReceived) {
            assertNotSame(eventType, event.getElementEvent());
        }
    }

    private void waitToExceedElementMaxIdle() throws InterruptedException {
        Thread.sleep(ELEMENT_MAX_IDLE * SECOND);
    }


    private void makeCacheFireSpoolToDiskEvent(JCS jcs, Map<String, String> elementsPutIntoCache) throws CacheException, InterruptedException {
        jcs.freeMemoryElements(elementsPutIntoCache.size());
        waitForEventsToBeFired();
    }

    private JCS loadJcsForRegion(String regionName) throws CacheException {
        return JCS.getInstance(regionName);
    }

    private void waitForEventsToBeFired() throws InterruptedException {
        Thread.sleep(3 * SECOND);
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