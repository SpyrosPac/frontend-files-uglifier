package com.github.spyrospac;

/**
 * Constant strings to keep test pom config xml and minified files locations
 */
class TestConstants {
    static final String ARTIFACT_ID = "frontend-files-uglifier";
    // POM locations
    static final String TEST_POM_LOCATION =
        "src/test/resources/unit/basic-test/basic-test-plugin-config.xml";
    static final String TEST_POM_LOCATION2 =
        "src/test/resources/unit/basic-test/test-plugin-config-keepname-true-replace-existing.xml";
    static final String TEST_POM_LOCATION3 =
        "src/test/resources/unit/basic-test/test-plugin-config-output-directory.xml";
    static final String TEST_POM_LOCATION4 =
            "src/test/resources/unit/basic-test/test-plugin-config-mangle.xml";
    static final String TEST_POM_LOCATION5 =
            "src/test/resources/unit/basic-test/test-plugin-config-skip-if-not-updated.xml";
    static final String TEST_POM_LOCATION6 =
            "src/test/resources/unit/basic-test/test-plugin-config-do-not-skip-if-updated.xml";
    static final String TEST_POM_LOCATION7 =
            "src/test/resources/unit/basic-test/test-plugin-config-do-not-skip-if-filename-contains-js.xml";
    static final String TEST_POM_LOCATION8 =
            "src/test/resources/unit/basic-test/basic-test-plugin-config-css.xml";

    // minified files locations
    static final String MINIFIED_FILE_LOCATION =
        "src/test/resources/script/uglifyJavascriptTest.min.js";
    static final String MINIFIED_FILE_LOCATION2=
        "src/test/resources/unit/basic-test/script/uglifyJavascriptTest.min.js";
    static final String MINIFIED_FILE_LOCATION3 =
        "src/test/resources/unit/basic-test/script/testToBeReplaced.js";
    static final String MINIFIED_FILE_LOCATION4 =
        "src/test/resources/unit/basic-test/script/testToBeReplaced.min.js";
    static final String MINIFIED_FILE_LOCATION5 =
        "src/test/resources/unit/basic-test/outputDirectory/uglifyJavascriptTest.min.js";
    static final String MINIFIED_FILE_LOCATION6 =
            "src/test/resources/unit/basic-test/script/testSkipMinify.js";
    static final String MINIFIED_FILE_LOCATION7 =
            "src/test/resources/unit/basic-test/script/testSkipMinify.min.js";
    static final String MINIFIED_FILE_LOCATION8 =
            "src/test/resources/unit/basic-test/script/testDoNotSkipMinify.js";
    static final String MINIFIED_FILE_LOCATION9 =
            "src/test/resources/unit/basic-test/script/testDoNotSkipMinify.min.js";
    static final String MINIFIED_FILE_LOCATION10 =
            "src/test/resources/unit/basic-test/script/test.jscript.min.js";
    static final String MINIFIED_FILE_LOCATION11 =
            "src/test/resources/unit/basic-test/css/testcss.min.css";
    static final String MINIFIED_FILE_LOCATION12 =
            "src/test/resources/css/uglifyJavascriptTest.js";

}
