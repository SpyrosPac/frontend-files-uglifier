# frontend-files-uglifier

Welcome! **frontend-files-uglifier** is a maven plugin to uglify (minify) front end files for Java Web Applications. Currently only JavaScript files can be minified.

# Introduction

Frontend files uglifier minifies your project JavaScript files into the same or a different directory, you can add a "min" suffix or replace the existing file. An option is also given to minify only files that are updated later than their minified copies (which are identified by *.min.js).
Goal is named **uglify**.

## Parameters

Name             |Type    |Description
-----------------|--------|--------------------------------------
sources          |FileSet |The directory containing javascript source files. (required)
outputDirectory  |String  |The output directory to put uglified files. If skipped, the minified file will be created on the same folder.
mangle|boolean  |Parameter for mangle (e.g. keep the function parameter names). Default value is true.
keepName|boolean  |Parameter to minify into a new file with the same name, if set to true. If no output directory is defined, then the existing file will be replaced.  Default value is false.
minifyOnlyUpdated|boolean  |Parameter to minify only js files that are modified after their respective js files. Default value is false.

## Example

You can call the plugin during the build process:

    <plugin>  
	    <groupId>com.github.spyrospac</groupId>  
	    <artifactId>frontend-files-uglifier</artifactId>  
	    <version>1.0</version>  
	  
	    <executions>  
	        <execution>  
	            <id>uglifyjs3</id>  
	            <goals>  
	                <goal>uglify</goal>  
	            </goals>  
	            <configuration>  
	                <sources>  
	                    <directory>web/src/main/webapp/resources/static/jscript</directory>  
	                    <excludes>  
	                        <exclude>org/foo</exclude>  
	                        <exclude>org/bar</exclude>  
	                        <exclude>**/*.min.js</exclude>  
	                    </excludes>  
	                </sources>  
	                <minifyOnlyUpdated>true</minifyOnlyUpdated>  
	            </configuration>  
	        </execution>  
	    </executions>  
	</plugin>



# Acknowledgement

The development of this plugin was done to check on the mojos with TDD. It is based on [uglifyjs-maven-plugin](https://github.com/tqh/uglifyjs-maven-plugin), which is not developed anymore. The plugin uses a minified version of the great [UglifyJS](https://github.com/mishoo/UglifyJS)
