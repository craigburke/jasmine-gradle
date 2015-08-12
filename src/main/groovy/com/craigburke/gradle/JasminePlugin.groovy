package com.craigburke.gradle

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.task.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

class JasminePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply NodePlugin
        NodeExtension nodeConfig = project.extensions.findByName('node')
        nodeConfig.download = true

        final String NPM_OUTPUT_PATH = project.file(nodeConfig.nodeModulesDir).absolutePath.replace(File.separator, '/') + '/node_modules/'
        final File KARMA_EXEC = project.file(NPM_OUTPUT_PATH + '/karma/bin/karma')
        final File KARMA_CONFIG = project.file("${project.buildDir.absolutePath}/karma.conf.js")
        
        def jasmineConfig = project.extensions.create('jasmine', JasmineModuleExtension)
        
        boolean grailsPluginApplied = project.extensions.findByName('grails')
        jasmineConfig.basePath = grailsPluginApplied ? 'grails-app/assets/' : 'src/assets/'

        def generateKarmaConfig = {
            KARMA_CONFIG.parentFile.mkdirs()
            KARMA_CONFIG.text = jasmineConfig.configJavaScript
        }
        
        project.task('jasmineDependencies', type: NpmTask, description: 'Installs dependencies needed for running Jasmine tests.')  {
            args = ['install'] + jasmineConfig.dependencies + ['--silent']
            
            outputs.files getDependencyPaths(NPM_OUTPUT_PATH, jasmineConfig.dependencies)
        }

        project.task('jasmineRefresh', group: 'Jasmine',
                description: 'Refreshes the generated karma config file') {
            doLast {
                generateKarmaConfig()
            }
        }
        
        project.task('jasmineGenerateConfig', description: 'Generates the karma config file', ) {
            outputs.file KARMA_CONFIG
            doLast {
                generateKarmaConfig()
            }
        }
        
        project.task('jasmineRun', type: NodeTask, dependsOn: ['jasmineDependencies', 'jasmineGenerateConfig'], group: 'Jasmine',
                 description: 'Executes jasmine tests') {
            script = KARMA_EXEC
            args = ['start', KARMA_CONFIG.absolutePath, '--single-run']
        }

        project.task('jasmineWatch', type: NodeTask, dependsOn: ['jasmineDependencies', 'jasmineGenerateConfig'], group: 'Jasmine',
                description: 'Executes jasmine tests in watch mode') {
            script = KARMA_EXEC
            args = ['start', KARMA_CONFIG.absolutePath, '--auto-watch']
        }

        project.task('jasmineClean', type: Delete, group: 'Jasmine',
                description: 'Deletes the generated karma config file and removes the node dependencies') {
            delete KARMA_CONFIG
            delete getDependencyPaths(NPM_OUTPUT_PATH, jasmineConfig.dependencies)
        }
    }
    
    String[] getDependencyPaths(String npmPath, dependencies) {
        dependencies.collect { "${npmPath}/${it.split('@')[0]}" }
    }
    

}