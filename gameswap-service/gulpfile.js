var gulp = require('gulp'),
    connect = require('gulp-connect');

gulp.task('connect', function() {
    connect.server({
        root: 'src/main/resources/assets/app',
        livereload: true,
        port: 9000
    });
});

gulp.task('html', function () {
    gulp.src('./src/main/resources/assets/app/*.html')
        .pipe(connect.reload());
});

gulp.task('watch', function () {
    gulp.watch(['./src/main/resources/assets/app/*.html'], ['html']);
});

gulp.task('default', ['connect', 'watch']);
