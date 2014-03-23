package net.kinjouj.gradle.plugin

import java.util.jar.JarEntry
import java.util.jar.JarFile

import org.gradle.api.DefaultTask;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.TaskAction;

class MochaPhantomJSTask extends DefaultTask {

    public static final String TASK_NAME = "mocha-phantomjs"
    private static final String TEST_RESOURCE_DIRECTORY_BASE = "/resources/test/"
    private String runner = "mocha.html"

    SourceDirectorySet mainSource;
    SourceDirectorySet testSource;

    @TaskAction
    public void runTask() {
        String buildTestResourceDir = project.buildDir.absolutePath + TEST_RESOURCE_DIRECTORY_BASE

        project.buildscript.configurations.classpath.each { artifact ->
            JarFile jar = null;

            try {
                jar = new JarFile(artifact)

                copyPluginResource(
                    jar,
                    jar.getEntry("mocha.js"),
                    project.file(buildTestResourceDir + "mocha.js")
                )

                copyPluginResource(
                    jar,
                    jar.getEntry("mocha.css"),
                    project.file(buildTestResourceDir + "mocha.css")
                )

                copyPluginResource(
                    jar,
                    jar.getEntry("chai.js"),
                    project.file(buildTestResourceDir + "chai.js")
                )
            } finally {
                if (jar != null) {
                    jar.close()
                }
            }
        }

        File runnerFile = project.file buildTestResourceDir + runner
        if (!runnerFile.exists()) {
            throw new IOException(runnerFile.toString() + " file not exists")
        }

        project.copy {
            from mainSource
            into buildTestResourceDir + "main"
            include "**/*.js"
        }

        project.copy {
            from testSource
            into buildTestResourceDir + "tests"
            include "**/*.js"
        }

        project.exec {
            commandLine = ["mocha-phantomjs", runnerFile.toString()]
        }
    }

    public void setFileResolver(FileResolver fileResolver) {
        mainSource = new DefaultSourceDirectorySet("javascript sources", fileResolver)
        mainSource.srcDir "src/main/javascript"
        mainSource.filter.include "**/*.js"

        testSource = new DefaultSourceDirectorySet("javascript test sources", fileResolver)
        testSource.srcDir "src/test/javascript"
        testSource.filter.include "**/*.js"
    }

    public void setRunner(String runner) {
        this.runner = runner
    }

    void copyPluginResource(JarFile jar, JarEntry entry, File outFile) {
        if (outFile.exists()) {
            return
        }

        FileWriter out = null

        try {
            InputStream is = null

            try {
                is = jar.getInputStream entry
                out = new FileWriter(outFile)
                is.eachLine { line ->
                    out.println line
                }
            } catch (IOException e) {
                e.printStackTrace()
            } finally {
                if (is != null) {
                    is.close()
                }
            }
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                out.close()
            }
        }
    }
}