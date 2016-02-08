angular.module('gameswap')
  .controller('LogoutCtrl', function($state, $auth, toastr) {
    if (!$auth.isAuthenticated()) { return; }
    $auth.logout()
      .then(function() {
        toastr.info('You have been logged out');
        $state.go("home");
      });
  });