angular.module('gameswap')
    .controller('NavbarCtrl', function ($scope, $auth, $state) {
        $scope.isAuthenticated = function () {
            return $auth.isAuthenticated();
        };

        $(".button-collapse").sideNav();
        $scope.click = function (link) {
            $state.go(link);
            $(".button-collapse").sideNav('hide');
        }
    });