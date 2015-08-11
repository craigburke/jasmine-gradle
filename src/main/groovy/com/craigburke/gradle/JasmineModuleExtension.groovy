package com.craigburke.gradle

import groovy.json.JsonBuilder

class JasmineModuleExtension {
    
    List dependencies = ['karma', 'karma-jasmine@2_0', 'karma-phantomjs-launcher', 'phantomjs']

    String basePath = ''
    List frameworks = ['jasmine']
    List browsers = ['PhantomJS']
    List files = []
    List excludes = []
    Map additional = [:]
    
    String getConfigJavaScript() {
        Map properties = [basePath: "../${basePath}", files: files, browsers: browsers,
                          frameworks: frameworks, excludes: excludes ]

        additional.each{ properties[it.key] = it.value }

        def json = new JsonBuilder()
        json(properties)
        String configJson = json.toString()
        
        "module.exports = function(config) { config.set(${configJson}) };"
    }
}

