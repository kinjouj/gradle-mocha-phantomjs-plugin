package net.kinjouj.gradle.plugin

import javax.inject.Inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver

class MochaPhantomJSPlugin implements Plugin<Project> {

    FileResolver fileResolver

    @Inject
    MochaPhantomJSPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    @Override
    public void apply(Project project) {
        MochaPhantomJSTask task = (MochaPhantomJSTask)project.task(
            MochaPhantomJSTask.TASK_NAME,
            type: MochaPhantomJSTask
        )
        task.fileResolver = fileResolver
    }
}