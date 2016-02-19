// Karma configuration
// Generated on Sat Feb 06 2016 09:36:14 GMT+1300 (NZDT)

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: 'src/main/resources/assets/',


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'app/bower_components/angular/angular.js',
            'app/bower_components/angular-mocks/angular-mocks.js',
            'app/bower_components/lodash/lodash.min.js',
            'app/bower_components/angular-animate/angular-animate.min.js',
            'app/bower_components/angular-messages/angular-messages.min.js',
            'app/bower_components/angular-resource/angular-resource.js',
            'app/bower_components/angular-ui-router/release/angular-ui-router.min.js',
            'app/bower_components/angular-toastr/dist/angular-toastr.tpls.min.js',
            'app/bower_components/restangular/dist/restangular.min.js',
            'app/bower_components/jquery/dist/jquery.min.js',
            'app/bower_components/Materialize/dist/js/materialize.min.js',
            'app/bower_components/satellizer/satellizer.min.js',
            'app/js/app.js',
            'app/js/**/*.js',
            'test/**/*Spec.js'
        ],


        // list of files to exclude
        exclude: [],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {},

        plugins: [
            'karma-phantomjs-launcher',
            'karma-jasmine'
        ],

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,

        // Concurrency level
        // how many browser should be started simultaneous
        concurrency: Infinity
    })
}
