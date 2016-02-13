angular.module("gameswap")
    .config(function(toastrConfig) {
    angular.extend(toastrConfig, {
        autoDismiss: true,
        containerId: 'toast-container',
        maxOpened: 0,
        newestOnTop: true,
        positionClass: 'toast-top-right',
        preventDuplicates: true,
        preventOpenDuplicates: false,
        target: 'body'
    });
});
