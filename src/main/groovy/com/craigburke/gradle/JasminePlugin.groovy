package com.craigburke.gradle

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.task.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete

class JasminePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply NodePlugin
        def nodeConfig = project.extensions.findByName('node')

        final String NPM_OUTPUT_PATH = project.file(nodeConfig.nodeModulesDir).absolutePath.replace(File.separator, '/') + '/node_modules/'
        final File KARMA_EXEC = project.file(NPM_OUTPUT_PATH + '/karma/bin/karma')
        final File KARMA_CONFIG = project.file("${project.buildDir.absolutePath}/karma.conf.js")
        
        def jasmineConfig = project.extensions.create('jasmine', JasmineModuleExtension)

        boolean grailsPluginApplied = project.extensions.findByName('grails')
        jasmineConfig.basePath = grailsPluginApplied ? '../grails-app/assets/libs/' : '../src/assets/'

        Task jasmineDependencies = project.task('jasmineDependencies',
                type: NpmTask,
                group: 'Jasmine',
                description: 'Installs dependencies needed for running Jasmine tests.')

        jasmineDependencies.configure {
            args = ['install'] + jasmineConfig.dependencies + ['--silent']
            outputs.files jasmineConfig.dependencies.collect { "${NPM_OUTPUT_PATH}/${it.split('@')[0]}" } + KARMA_CONFIG
        }
        jasmineDependencies.doLast {
            KARMA_CONFIG.parentFile.mkdirs()
            KARMA_CONFIG.text = jasmineConfig.configJavaScript
        }

        Task jasmineRun = project.task('jasmineRun',
                 type: NodeTask, dependsOn: 'jasmineDependencies', group: 'Jasmine',
                 description: 'Executes jasmine tests')
        
        jasmineRun.configure {
            script = KARMA_EXEC
            args = ['start', KARMA_CONFIG.absolutePath, '--single-run']
        }

        Task jasmineWatch = project.task('jasmineWatch',
                type: NodeTask, dependsOn: 'jasmineDependencies', group: 'Jasmine',
                description: 'Executes jasmine tests in watch mode')

        jasmineWatch.configure {
            script = KARMA_EXEC
            args = ['start', KARMA_CONFIG.absolutePath, '--auto-watch']
        }

        Task jasmineClean = project.task('jasmineClean',
                type: Delete, group: 'Jasmine',
                description: 'Deletes the generated karma config file')
        jasmineClean.configure {
            delete KARMA_CONFIG
        }

    }

}