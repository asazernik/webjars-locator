package org.webjars;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RequireJSTest {

    private static String WEBJAR_URL_PREFIX = "/webjars/";
    private static String WEBJAR_CDN_PREFIX = "http://cdn.jsdelivr.net/webjars/";

    private static ObjectNode getPackageConfig(Map<String, ObjectNode> allJson, String name) {
        return (ObjectNode) allJson.get(name).get("packages").get(0);
    }

    @Test
    public void should_generate_correct_javascript() {
        String javaScript = RequireJS.getSetupJavaScript(WEBJAR_URL_PREFIX);

        assertTrue(javaScript.indexOf("\"bootstrap\":\"3.1.1\"") > 0);
    }

    @Test
    public void should_generate_correct_json() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);
        Map<String, ObjectNode> jsonWithCdn = RequireJS.getSetupJson(WEBJAR_CDN_PREFIX, WEBJAR_URL_PREFIX);

        assertEquals(WEBJAR_URL_PREFIX + "jquery/2.1.0/jquery", jsonNoCdn.get("jquery").get("paths").withArray("jquery").get(0).asText());
        assertEquals(WEBJAR_CDN_PREFIX + "jquery/2.1.0/jquery", jsonWithCdn.get("jquery").get("paths").withArray("jquery").get(0).asText());
        assertEquals(WEBJAR_URL_PREFIX + "jquery/2.1.0/jquery", jsonWithCdn.get("jquery").get("paths").withArray("jquery").get(1).asText());

        assertEquals("$", jsonNoCdn.get("jquery").get("shim").get("jquery").get("exports").asText());
    }

    @Test
    public void should_get_nonversioned_json() {
        List<Map.Entry<String, Boolean>> prefixes = new ArrayList<Map.Entry<String, Boolean>>();
        prefixes.add(new AbstractMap.SimpleEntry<String, Boolean>(WEBJAR_URL_PREFIX, false));

        Map<String, ObjectNode> json = RequireJS.generateSetupJson(prefixes);

        assertEquals(WEBJAR_URL_PREFIX + "jquery/jquery", json.get("jquery").get("paths").withArray("jquery").get(0).asText());
    }

    @Test
    public void should_replace_location() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);

        assertEquals(WEBJAR_URL_PREFIX + "when-node/3.5.2/when", jsonNoCdn.get("when-node").withArray("packages").get(0).get("location").asText());

        Map<String, ObjectNode> jsonWithCdn = RequireJS.getSetupJson(WEBJAR_CDN_PREFIX, WEBJAR_URL_PREFIX);

        assertEquals(WEBJAR_URL_PREFIX + "when-node/3.5.2/when", jsonWithCdn.get("when-node").withArray("packages").get(0).get("location").asText());
    }

    @Test
    public void should_work_with_bower_webjars() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);

        // check inclusion of a transitive dependency
        // todo: the angular version changes due to a range transitive dependency
        ObjectNode angularPackage = getPackageConfig(jsonNoCdn, "angular");
        assertEquals(WEBJAR_URL_PREFIX + "angular/1.4.4/", angularPackage.get("location").get(0).asText());
        assertEquals("angular", angularPackage.get("main").asText());

        // full check of the direct dependency
        String expectedPath = "angular-bootstrap/0.13.0/";
        ObjectNode packageNoCdn = getPackageConfig(jsonNoCdn, "angular-bootstrap");

        assertEquals(1, packageNoCdn.get("location").size());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageNoCdn.get("location").get(0).asText());
        assertEquals("ui-bootstrap-tpls", packageNoCdn.get("main").asText());

        Map<String, ObjectNode> jsonWithCdn = RequireJS.getSetupJson(WEBJAR_CDN_PREFIX, WEBJAR_URL_PREFIX);
        ObjectNode packageWithCdn = getPackageConfig(jsonWithCdn, "angular-bootstrap");

        assertEquals(2, packageWithCdn.get("location").size());
        assertEquals(WEBJAR_CDN_PREFIX + expectedPath, packageWithCdn.get("location").get(0).asText());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageWithCdn.get("location").get(1).asText());
        assertEquals("ui-bootstrap-tpls", packageWithCdn.get("main").asText());
    }

    @Test
    public void should_pick_right_script_in_bower_webjars() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);
        ObjectNode packageNoCdn = getPackageConfig(jsonNoCdn, "angular-schema-form");
        String expectedPath = "angular-schema-form/0.8.2/";

        assertEquals(1, packageNoCdn.get("location").size());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageNoCdn.get("location").get(0).asText());
        assertEquals("dist/schema-form", packageNoCdn.get("main").asText());

        Map<String, ObjectNode> jsonWithCdn = RequireJS.getSetupJson(WEBJAR_CDN_PREFIX, WEBJAR_URL_PREFIX);
        ObjectNode packageWithCdn = getPackageConfig(jsonWithCdn, "angular-schema-form");

        assertEquals(2, packageWithCdn.get("location").size());
        assertEquals(WEBJAR_CDN_PREFIX + expectedPath, packageWithCdn.get("location").get(0).asText());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageWithCdn.get("location").get(1).asText());
        assertEquals("dist/schema-form", packageWithCdn.get("main").asText());
    }

    @Test
    public void should_work_with_npm_webjars() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);
        ObjectNode packageNoCdn = getPackageConfig(jsonNoCdn, "angular-pouchdb");
        String expectedPath = "angular-pouchdb/2.0.8/";

        assertEquals(1, packageNoCdn.get("location").size());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageNoCdn.get("location").get(0).asText());
        assertEquals("dist/angular-pouchdb", packageNoCdn.get("main").asText());

        Map<String, ObjectNode> jsonWithCdn = RequireJS.getSetupJson(WEBJAR_CDN_PREFIX, WEBJAR_URL_PREFIX);
        ObjectNode packageWithCdn = getPackageConfig(jsonWithCdn, "angular-pouchdb");

        assertEquals(2, packageWithCdn.get("location").size());
        assertEquals(WEBJAR_CDN_PREFIX + expectedPath, packageWithCdn.get("location").get(0).asText());
        assertEquals(WEBJAR_URL_PREFIX + expectedPath, packageWithCdn.get("location").get(1).asText());
        assertEquals("dist/angular-pouchdb", packageWithCdn.get("main").asText());
    }

    @Test
    public void should_fix_npm_module_names() {
        Map<String, ObjectNode> jsonNoCdn = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);

        assertEquals("validate-js", getPackageConfig(jsonNoCdn, "validate.js").get("name").asText());
    }

    @Test
    public void should_be_empty_if_no_main() {
        Map<String, ObjectNode> json = RequireJS.getSetupJson(WEBJAR_URL_PREFIX);
        ObjectNode packageConf = getPackageConfig(json, "babel-runtime");
        assertNull(packageConf.get("main"));
        assertEquals(1, packageConf.get("location").size());
        assertEquals(WEBJAR_URL_PREFIX + "babel-runtime/5.8.19/", packageConf.get("location").get(0).asText());
    }

}
