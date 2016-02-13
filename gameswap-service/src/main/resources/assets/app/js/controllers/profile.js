angular.module('gameswap')
    .controller('ProfileCtrl', function ($scope, $auth, Restangular, $state) {
        if(!$auth.isAuthenticated()) {
            $state.go("login");
        }
        var userId = $auth.getPayload().sub;
        $scope.user = {};
        Restangular.setBaseUrl("/gameswap");
        Restangular.one("profile", userId).get().then(function(user){
            $scope.user = user;
        });

        $scope.updateProfile =  function () {
            $scope.user.save();
        }
    });
