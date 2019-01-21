package com.github.spyrospac;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;
import java.nio.charset.Charset;

import static com.github.spyrospac.TestConstants.*;

public class UglifyMojoTest extends AbstractMojoTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        deleteFileIfExists(MINIFIED_FILE_LOCATION);

        deleteFileIfExists(MINIFIED_FILE_LOCATION2);

        deleteFileIfExists(MINIFIED_FILE_LOCATION3);

        deleteFileIfExists(MINIFIED_FILE_LOCATION4);

        deleteFileIfExists(MINIFIED_FILE_LOCATION5);

        deleteFileIfExists(MINIFIED_FILE_LOCATION6);

        deleteFileIfExists(MINIFIED_FILE_LOCATION7);

        deleteFileIfExists(MINIFIED_FILE_LOCATION8);

        deleteFileIfExists(MINIFIED_FILE_LOCATION9);

        deleteFileIfExists(MINIFIED_FILE_LOCATION10);

        super.tearDown();

    }

    /**
     * tests basic minification of JS file
     *
     * @throws Exception
     */
    public void testBasicMinify() throws Exception {

        File testPom = new File(getBasedir(), TEST_POM_LOCATION);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Test Basic Minify");

        mojo.execute();

        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION2);

        assertTrue(minifiedFile.exists());

        String minifiedString = FileUtils.readFileToString(minifiedFile, Charset.defaultCharset());

        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);
    }

    /**
     * Tests basic minification of JS file by replacing the existing one.
     * The file that is replaced is created during the test. Deleted on tear down.
     *
     * @throws Exception
     */
    public void testMinifyAndReplaceFile() throws Exception {

        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION3);
        String txt = "uglifyJavascript = function (code) {\n" +
                "\n" +
                "var result =  UglifyJS.minify(code);\n" +
                "  return result.code;\n" +
                "};\n";

        FileUtils.write(minifiedFile, txt, Charset.defaultCharset());
        long lastModifiedTime = minifiedFile.lastModified();
        assertTrue(minifiedFile.exists());

        File testPom = new File(getBasedir(), TEST_POM_LOCATION2);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Test Minify And Replace File");

        mojo.execute();

        // the existing file was modified
        minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION3);
        assertFalse(lastModifiedTime>minifiedFile.lastModified());

        // no testToBeReplaced.min.js file was created
        File minifiedFile2 = new File(getBasedir(), MINIFIED_FILE_LOCATION4);
        assertFalse(minifiedFile2.exists());

        // minification was successful
        String minifiedString = FileUtils.readFileToString(minifiedFile, Charset.defaultCharset());
        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);
    }

    /**
     * Tests basic minification of JS file to a specified directory.
     *
     * @throws Exception
     */
    public void testMinifyToSpecifiedDirectory() throws Exception {

        File testPom = new File(getBasedir(), TEST_POM_LOCATION3);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Test Minify To Specified Directory");

        mojo.execute();

        // the existing file was modified
        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION5);
        assertTrue(minifiedFile.exists());

        // minification was successful
        String minifiedString = FileUtils.readFileToString(minifiedFile, Charset.defaultCharset());
        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);
    }

    /**
     * Tests minification with mangle set to off.
     *
     * @throws Exception
     */
    public void testMinifyWithoutMangle() throws Exception {
        final String EXPECTED_CONTENT = "uglifyJavascript=function(code){return UglifyJS.minify(code).code};";

        File testPom = new File(getBasedir(), TEST_POM_LOCATION4);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));
        mojo.getLog().info("-- Test Minify To Specified Directory");

        mojo.execute();

        assertNotNull(mojo);

        // the existing file was modified
        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION2);
        assertTrue(minifiedFile.exists());

        // minification was successful
        String minifiedString = FileUtils.readFileToString(minifiedFile, Charset.defaultCharset());
        assertEquals(EXPECTED_CONTENT, minifiedString);
    }

    /**
     * Tests that minification of file is skipped if the original file is not updated later than an already existing
     * minified file with the same name + min.js.
     * Flag for minify if updated is set to true in plugin config xml.
     *
     * @throws Exception
     */
    public void testSkipMinifiedIfNotUpdated() throws Exception {

        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION6);
        String txt = "uglifyJavascript = function (code) {\n" +
                "\n" +
                "var result =  UglifyJS.minify(code);\n" +
                "  return result.code;\n" +
                "};\n";

        FileUtils.write(minifiedFile, txt, Charset.defaultCharset());
        assertTrue(minifiedFile.exists());
        long lastModified = minifiedFile.lastModified();


        File testPom = new File(getBasedir(), TEST_POM_LOCATION5);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Test Skip Minified If Not Updated");

        mojo.execute();

        // no testToBeReplaced.min.js file was created
        File minifiedFile2 = new File(getBasedir(), MINIFIED_FILE_LOCATION7);
        assertTrue(minifiedFile2.exists());
        long lastModified2 = minifiedFile2.lastModified();

        assertTrue(lastModified2>lastModified);

        // minification was successful
        String minifiedString = FileUtils.readFileToString(minifiedFile2, Charset.defaultCharset());
        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);

        mojo.execute();
        minifiedFile2 = new File(getBasedir(), MINIFIED_FILE_LOCATION7);
        assertTrue(minifiedFile2.exists());
        long lastModified3 = minifiedFile2.lastModified();

        assertEquals(lastModified2,lastModified3);
    }

    /**
     * Tests basic minification of JS file when an existing minified file exists, but the original was updated.
     * Flag for minify if updated is set to true in plugin config xml.
     *
     * @throws Exception
     */
    public void testDoNotSkipMinificationIfUpdated() throws Exception {

        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION9);
        String txt = "uglifyJavascript = function (code) {\n" +
                "\n" +
                "var result =  UglifyJS.minify(code);\n" +
                "  return result.code;\n" +
                "};\n";

        FileUtils.write(minifiedFile, txt, Charset.defaultCharset());
        assertTrue(minifiedFile.exists());
        long minifiedLastModified = minifiedFile.lastModified();

        Thread.sleep(1);
        deleteFileIfExists(MINIFIED_FILE_LOCATION8);

        File file = new File(getBasedir(), MINIFIED_FILE_LOCATION8);
        FileUtils.write(file, txt, Charset.defaultCharset());
        assertTrue(file.exists());
        long lastModified = file.lastModified();

        assertTrue(lastModified >= minifiedLastModified);

        File testPom = new File(getBasedir(), TEST_POM_LOCATION6);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Test Do Not Skip Minification If Updated");

        mojo.execute();

        // no testToBeReplaced.min.js file was created
        File minifiedFile2 = new File(getBasedir(), MINIFIED_FILE_LOCATION9);
        assertTrue(minifiedFile2.exists());
        long minifiedLastModified2 = minifiedFile2.lastModified();

        assertTrue(minifiedLastModified2>minifiedLastModified);

        // minification was successful
        String minifiedString = FileUtils.readFileToString(minifiedFile2, Charset.defaultCharset());
        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);
    }

    /**
     * tests that if a file contains '.js' in its filename it is not replaced
     *
     * @throws Exception
     */
    public void testDoNotSkipIfFilenameContainsDotJs() throws Exception {

        File testPom = new File(getBasedir(), TEST_POM_LOCATION7);

        UglifyMojo mojo = new UglifyMojo();
        mojo = (UglifyMojo) configureMojo(
                mojo, extractPluginConfiguration("uglifyjs3-maven-plugin", testPom
                ));

        assertNotNull(mojo);

        mojo.getLog().info("-- Do Not Skip If Filename Contains Dot Js");

        mojo.execute();

        File minifiedFile = new File(getBasedir(), MINIFIED_FILE_LOCATION10);

        assertTrue(minifiedFile.exists());

        String minifiedString = FileUtils.readFileToString(minifiedFile, Charset.defaultCharset());

        assertEquals("uglifyJavascript=function(i){return UglifyJS.minify(i).code};", minifiedString);
    }

    private void deleteFileIfExists(String location) {
        File file = new File(getBasedir(), location);
        if (file.exists()) {
            file.delete();
        }
    }
}

