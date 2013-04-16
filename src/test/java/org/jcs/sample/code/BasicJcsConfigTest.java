package org.jcs.sample.code;


import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.junit.Test;

public class BasicJcsConfigTest {


    @Test
    public void testAddListener() throws CacheException {

        JCS jcs = JCS.getInstance("testCache1");

        MyEventHandler meh = new MyEventHandler();

        // jcs.getDefaultElementAttributes returns a copy not a reference
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler(meh);
        jcs.put("key", "data", attributes);

        jcs.freeMemoryElements(1);
//     jcs.remove("key");
        jcs.clear();
    }

}

class MyEventHandler implements IElementEventHandler {

    @Override
    public void handleElementEvent(IElementEvent event) {
        System.out.println("Received event: " + event.getElementEvent());
    }
}
