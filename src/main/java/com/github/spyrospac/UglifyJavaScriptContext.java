package com.github.spyrospac;

/*
 * Copyright 2019 Spyros Pachomis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Class used to invoke uglify javascript file.
 */
class UglifyJavaScriptContext {
    private final Context cx = Context.enter();
    private final ScriptableObject global = cx.initStandardObjects();

    UglifyJavaScriptContext(final Log log, final String... scripts) {
        ClassLoader cl = getClass().getClassLoader();
        for (String script : scripts) {

            try (InputStreamReader in = new InputStreamReader(cl.getResourceAsStream("script/" + script), "UTF-8")) {
                cx.evaluateReader(global, in, script, 1, null);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    /**
     * Invokes the uglifyJavascript() function with parameters the code and the mangle option.
     *
     * @param file
     * @param mangle
     * @return minified file as a String
     * @throws IOException
     */
    String invokeFunctionOnFile(final File file, final boolean mangle) throws IOException {
        String data = FileUtils.readFileToString(file, "UTF-8");
        ScriptableObject.putProperty(global, "data", data);
        ScriptableObject.putProperty(global, "mangle", String.valueOf(mangle));
        return cx.evaluateString(global,
                "uglifyJavascript" + "(String(data), String(mangle));", "<cmd>", 1, null).toString();
    }
}
