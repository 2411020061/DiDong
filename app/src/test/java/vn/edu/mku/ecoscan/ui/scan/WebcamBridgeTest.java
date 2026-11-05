package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebcamBridgeTest {
    @Test public void acceptsOnlyNumericIpv4HostsForLocalWebcamRequests() {
        assertTrue(WebcamBridge.validHost("10.0.2.2"));
        assertTrue(WebcamBridge.validHost("192.168.1.24"));
        assertFalse(WebcamBridge.validHost("192.168.1.999"));
        assertFalse(WebcamBridge.validHost("localhost"));
        assertFalse(WebcamBridge.validHost("10.0.2.2/path"));
        assertFalse(WebcamBridge.validHost("10.0.2.2:8080"));
    }
}
