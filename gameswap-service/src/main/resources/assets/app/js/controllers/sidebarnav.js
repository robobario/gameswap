angular.module('gameswap')
    .controller('sideNav', ["$scope", "$state", function($scope, $state) {
        $(".button-collapse").sideNav();
        $scope.click = function (link) {
            $state.go(link);
            $(".button-collapse").sideNav('hide');
        }

    }]);

