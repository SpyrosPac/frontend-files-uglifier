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
 * TODO uglify css too
 *
 * @goal uglify_plugin
 * @phase compile
 */
@Mojo(name = "uglify", defaultPhase = LifecyclePhase.COMPILE)
public class UglifyMojo extends AbstractMojo {

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

    private int uglify(File[] jsFiles) throws IOException {
        int count = 0;

        for (File jsFile : jsFiles) {
            File outputFile = getOutputFile(jsFile);

            boolean skipFile = shouldFileBeSkipped(outputFile, jsFile);

            if (!skipFile) {
                final String jsFilePath = jsFile.getPath();
                final String encoding = "UTF-8";

                getLog().debug("Uglifying " + jsFilePath);
                String output =
                        new UglifyJavaScriptContext(getLog(), "uglifyjs.js", "uglifyJavascript.js")
                                .invokeFunctionOnFile(jsFile, mangle);

                try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile, false), encoding)) {
                    out.write(output);
                } catch (IOException e) {
                    getLog().error("Could not uglify " + jsFile.getPath() + ".", e);
                    throw e;
                } finally {
                    Context.exit();
                }
                count++;
            } else {
                getLog().debug("skipping file " + jsFile.getName());
            }

        }

        return count;
    }

    /**
     * Creates the {@link java.io.File} instance of the new minified file.
     *
     * @param inputFile
     * @return a {@link java.io.File} instance
     * @throws IOException
     */
    private File getOutputFile(File inputFile) throws IOException {
        String fileName = keepName ? inputFile.getName() : inputFile.getName().replace(".js", ".min.js");

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
     * Returns {@link File directory} containing JavaScript source {@link File files}.
     *
     * @return {@link File Directory} containing JavaScript source {@link File files}
     */
    private File getSourceDir() {
        return new File(sources.getDirectory());
    }

    /**
     * Returns JavaScript sources {@link File files}.
     *
     * @return Array of JavaScript sources {@link File files}
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
     * Checks if the file should not be minified
     * If keepName is true and outputDirectory is null, don't skip, just replace.
     * If "minifyOnlyUpdated" parameter is set to true, then checks if the file is older than an existing min.js file
     * (which would mean that the original file is updated)
     *
     * @param outputFile
     * @param jsFile
     * @return true if the file should be skipped
     *
     */
    private boolean shouldFileBeSkipped(File outputFile, File jsFile) {
        return !jsFile.getName().endsWith(".js")
                || (outputFile.exists() && !outputFile.getAbsolutePath().equals(jsFile.getAbsolutePath())
                && (minifyOnlyUpdated && outputFile.lastModified() >= jsFile.lastModified()));
    }
}
