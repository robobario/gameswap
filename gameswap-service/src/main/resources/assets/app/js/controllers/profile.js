angular.module('gameswap')
    .controller('ProfileCtrl', function ($scope, $auth, Restangular, $state, toastr) {
        $scope.user = {};
        if(!$auth.isAuthenticated()) {
            $state.go("login");
        }
        var userId = $auth.getPayload().sub;
        Restangular.setBaseUrl("/gameswap");
        Restangular.one("users", userId).get().then(function(user){
            $scope.user = user;
        });

        $scope.updateProfile =  function () {
            $scope.user.save().then(function() {
                toastr.success("Profile updated");
            });

        };

        $scope.deleteAccount = function() {
            $('#confirmDeleteModal').openModal();
        };

        $scope.cancelDelete = function() {
            $('#confirmDeleteModal').closeModal();
        };

        $scope.confirmDelete = function() {
            $scope.user.remove().then(function() {
                $state.go("home");
                $auth.logout();
                $('#confirmDeleteModal').closeModal();
                toastr.success('Your account has been deleted');
            });
        };
    });
