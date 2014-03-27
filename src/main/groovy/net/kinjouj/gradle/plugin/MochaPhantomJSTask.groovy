package net.kinjouj.gradle.plugin

import java.util.jar.JarEntry
import java.util.jar.JarFile

import org.gradle.api.DefaultTask
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.TaskAction

class MochaPhantomJSTask extends DefaultTask {

    public static final String TASK_NAME = "testMochaPhantomJS"

    SourceDirectorySet mainSource
    SourceDirectorySet testSource

    String runner = "mocha.html"
    List<String> options = [];

    @TaskAction
    public void runTask() {
        def buildTestResourceDir = project.buildDir.absolutePath + "/resources/test"
        project.buildscript.configurations.classpath.each { artifact ->
            new JarFile(artifact).with { jar ->
                copyPluginResource(jar, project.file(buildTestResourceDir + "/mocha.js"))
                copyPluginResource(jar, project.file(buildTestResourceDir + "/mocha.css"))
                copyPluginResource(jar, project.file(buildTestResourceDir + "/chai.js"))
            }
        }

        File runnerFile = project.file(buildTestResourceDir + '/' + runner)
        if (!runnerFile.exists()) {
            throw new IOException(runnerFile.toString() + " file not exists")
        }

        project.copy {
            from mainSource
            into buildTestResourceDir + "/main"
        }

        project.copy {
            from testSource
            into buildTestResourceDir + "/tests"
        }

        project.exec {
            commandLine = buildCommand(runnerFile)
        }
    }

    public void setFileResolver(FileResolver fileResolver) {
        mainSource = new DefaultSourceDirectorySet("javascript main sources", fileResolver)
        mainSource.with {
            srcDir "src/main/javascript"
            include "**/*.js"
        }

        testSource = new DefaultSourceDirectorySet("javascript test sources", fileResolver)
        testSource.with {
            srcDir "src/test/javascript"
            include "**/*.js"
        }
    }

    public void setRunner(String runner) {
        this.runner = runner
    }

    public void setOptions(List<String> options) {
        this.options = options
    }

    void copyPluginResource(JarFile jar, File outFile) {
        if (outFile.exists()) return

        JarEntry entry = jar.getJarEntry(outFile.getName())

        if (entry) {
            jar.getInputStream(entry).withReader { is ->
                new FileWriter(outFile).withWriter { out ->
                    is.eachLine { line ->
                        out.println line
                    }
                }
            }
        }
    }

    Object buildCommand(File runnerFile) {
        def commands = ["mocha-phantomjs"]
        commands += options
        commands << runnerFile.toString()

        return commands
    }
}
