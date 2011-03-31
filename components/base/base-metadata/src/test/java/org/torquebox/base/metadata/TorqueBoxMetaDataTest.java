package org.torquebox.base.metadata;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;
import org.jruby.Ruby;


public class TorqueBoxMetaDataTest {
    
    Ruby ruby = Ruby.newInstance();

    @Test
    public void testOverlayingWebSection() {
        Map externalWeb = ruby.evalScriptlet( "{ 'web' => { 'context' => '/', 'value' => 'special' } }" ).convertToHash();
        Map internalWeb = ruby.evalScriptlet( "{ 'web' => { 'rackup' => 'foo.ru', 'value' => 'normal' } }" ).convertToHash();
        
        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(externalWeb);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internalWeb);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);

        Map<String,String> merged = (Map<String,String>) mergedMetaData.getSection("web");
        assertEquals("/", merged.get("context"));
        assertEquals("foo.ru", merged.get("rackup"));
        assertEquals("special", merged.get("value"));
    }
    
    @Test
    public void testOverlayingDeepNests() {
        Map external = ruby.evalScriptlet( "{ 'jobs' => { 'x' => { 'job' => 'Y' } } }" ).convertToHash();
        Map internal = ruby.evalScriptlet( "{ 'jobs' => { 'x' => { 'job' => 'X', 'cron' => '0 */5 * * * ?' } } }" ).convertToHash();
        
        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(external);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internal);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);

        Map<String,Map> merged = (Map<String,Map>) mergedMetaData.getSection("jobs");
        assertEquals("Y", merged.get("x").get("job"));
        assertEquals("0 */5 * * * ?", merged.get("x").get("cron"));
    }
    
}
