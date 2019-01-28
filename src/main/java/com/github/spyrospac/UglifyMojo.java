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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.mozilla.javascript.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Goal which uglifies JavaScript files with UglifyJS (version 3.4.9).
 *
 * @goal uglify_plugin
 * @phase compile
 */
@Mojo(name = "uglify", defaultPhase = LifecyclePhase.COMPILE)
public class UglifyMojo extends AbstractMojo {

    private static boolean minifyJSEnabled;
    private static boolean minifyCSSEnabled;

    UglifyMojo() {
        setMinifyCSSEnabled(false);
        setMinifyJSEnabled(false);
    }

    /**
     * {@link org.apache.maven.shared.model.fileset.FileSet} containing JavaScript source files.
     */
    @Parameter
    private FileSet sources;
    /**
     * {@link java.io.File} indicating where the minified files should be created.
     * If no output directory is defined, the minified file will be created on the same folder.
     */
    @Parameter
    private File outputDirectory;
    /**
     * Parameter for mangle (e.g. keep the function parameter names)
     * Default value is true.
     */
    @Parameter
    private boolean mangle = true;
    /**
     * Parameter to minify into a new file with the same name, if set to true.
     * If no output directory is defined, then the existing file will be replaced.
     * Default value is false.
     */
    @Parameter
    private boolean keepName = false;
    /**
     * Parameter to minify only js files that are modified after their respective js files.
     * Default value is false.
     */
    @Parameter
    private boolean minifyOnlyUpdated = false;
    /**
     * Parameter to identify files to minify.
     * Default value is javascript and css.
     */
    @Parameter
    private String typesToMinify = "js,css";

    public void execute() throws MojoExecutionException {

        try {
            long start = System.currentTimeMillis();
            int count = uglify(getSourceFiles());
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            getLog().info("Uglified " + count + " file(s) in " + timeElapsed + " milliseconds.");
        } catch (IOException e) {
            throw new MojoExecutionException("Failure to precompile handlebars templates.", e);
        }
    }

    private int uglify(File[] files) throws IOException {
        int count = 0;

        checkTypesToMinify();

        for (File file : files) {
            File outputFile = getOutputFile(file);

            boolean skipFile = shouldFileBeSkipped(outputFile, file);

            if (!skipFile) {
                final String filePath = file.getPath();

                if (isMinifyJSEnabled() && isFileJavaScript(file)) {
                    getLog().debug("Uglifying " + filePath);
                    String output =
                            new UglifyJavaScriptContext(getLog(), "uglifyjs.js", "uglifyJavascript.js")
                                    .invokeUglifyJSFunctionOnFile(file, mangle);

                    writeToFile(output, file, outputFile);
                    count++;
                } else if (isMinifyCSSEnabled() && isFileCSS(file)) {
                    getLog().debug("Uglifying " + filePath);
                    String output =
                            new CleanCSSJavaScriptContext(getLog(), "clean-css-v4.2.1.js", "minifyCSS.js")
                                    .invokeCleanCssFunctionOnFile(file);

                    writeToFile(output, file, outputFile);
                    count++;
                }

            } else {
                getLog().debug("skipping file " + file.getName());
            }

        }

        return count;
    }

    private void writeToFile(String output, File file, File outputFile) throws IOException{
        final String encoding = "UTF-8";

        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile, false), encoding)) {
            out.write(output);
        } catch (IOException e) {
            getLog().error("Could not uglify " + file.getPath() + ".", e);
            throw e;
        } finally {
            Context.exit();
        }
    }

    /**
     * Creates the {@link java.io.File} instance of the new minified file.
     *
     * @param inputFile
     * @return a {@link java.io.File} instance
     * @throws IOException
     */
    private File getOutputFile(File inputFile) throws IOException {
        // replace ending
        String fileName = keepName ? inputFile.getName() : inputFile.getName().replaceAll(".js$", ".min.js").replaceAll(".css$", ".min.css");

        if (outputDirectory == null) {
            // create the minified file in the same folder
            return new File(inputFile.getParent(), fileName);
        } else {
            String relativePath = getSourceDir().toURI().relativize(inputFile.getParentFile().toURI()).getPath();
            File outputBaseDir = new File(outputDirectory, relativePath);
            if (!outputBaseDir.exists()) {
                FileUtils.forceMkdir(outputBaseDir);
            }

            return new File(outputBaseDir, fileName);
        }

    }

    /**
     * Returns {@link File directory} containing JavaScript/CSS source {@link File files}.
     *
     * @return {@link File Directory} containing JavaScript/CSS source {@link File files}
     */
    private File getSourceDir() {
        return new File(sources.getDirectory());
    }

    /**
     * Returns JavaScript sources {@link File files}.
     *
     * @return Array of JavaScript/CSS sources {@link File files}
     */
    private File[] getSourceFiles() {
        getLog().info(sources.getDirectory());
        FileSetManager fileSetManager = new FileSetManager();
        String[] includedFiles = fileSetManager.getIncludedFiles(sources);
        File sourceDir = getSourceDir();
        File[] sourceFiles = new File[includedFiles.length];

        for (int i = 0; i < includedFiles.length; i++) {
            sourceFiles[i] = new File(sourceDir, includedFiles[i]);
        }
        return sourceFiles;
    }

    /**
     * Checks if the file should not be minified (only js and css files are allowed).
     * If keepName is true and outputDirectory is null, don't skip, just replace.
     * If "minifyOnlyUpdated" parameter is set to true, then checks if the file is older than an existing min.js file
     * (which would mean that the original file is updated)
     *
     * @param outputFile
     * @param inputFile
     * @return true if the file should be skipped
     */
    private boolean shouldFileBeSkipped(File outputFile, File inputFile) {
        return (!isFileJavaScript(inputFile) && !isFileCSS(inputFile))
                || (outputFile.exists() && !outputFile.getAbsolutePath().equals(inputFile.getAbsolutePath())
                && (minifyOnlyUpdated && outputFile.lastModified() >= inputFile.lastModified()));
    }

    private void checkTypesToMinify() {
        String[] values = typesToMinify.split(",");
        for (String value : values) {
            if ("js".equals(value)) {
                setMinifyJSEnabled(true);
            } else if ("css".equals(value)) {
                setMinifyCSSEnabled(true);
            }
        }
    }

    private boolean isFileJavaScript(File file) {
        return file.getName().endsWith(".js");
    }

    private boolean isFileCSS(File file) {
        return file.getName().endsWith(".css");
    }

    private boolean isMinifyJSEnabled() {
        return minifyJSEnabled;
    }

    private void setMinifyJSEnabled(boolean minifyJSEnabled) {
        UglifyMojo.minifyJSEnabled = minifyJSEnabled;
    }

    private boolean isMinifyCSSEnabled() {
        return minifyCSSEnabled;
    }

    private void setMinifyCSSEnabled(boolean minifyCSSEnabled) {
        UglifyMojo.minifyCSSEnabled = minifyCSSEnabled;
    }
}
