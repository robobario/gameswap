describe('LoginCtrl', function () {
    var $httpBackend, $rootScope, controller, $auth, $location;
    var loginUrl = '/gameswap/auth/logout';

    // Set up the module
    beforeEach(module('gameswap'));

    beforeEach(inject(function ($injector) {
        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');
        //// Get hold of a scope (i.e. the root scope)
        $rootScope = $injector.get('$rootScope');
        $auth = $injector.get('$auth');
        $auth.logout();
        $location = $injector.get('$location');
        //// The $controller service is used to create instances of controllers
        var $controller = $injector.get('$controller');
        var toastr = $injector.get('toastr');

        controller = $controller('LoginCtrl', {
            '$scope': $rootScope,
            '$location': $location,
            '$auth': $auth,
            'toastr': toastr
        });
    }));


    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });


    describe('logout()', function() {
        it('successful login followed by logout results in unauthenticated', function () {
            $httpBackend.when('POST', loginUrl).respond(200, {token: 'somelongtoken'});
            $httpBackend.expectPOST(loginUrl);
            $rootScope.login();
            $httpBackend.flush();
            expect($auth.isAuthenticated()).toEqual(true);
        });


        it('unser that is not signed in has no change on attempted logout', function () {
            //// Notice how you can change the response even after it was set
            $rootScope.logout();
            $httpBackend.when('POST', loginUrl).respond(401, {"code": 500, "message": "Wrong email and/or password"});
            $httpBackend.flush();
            expect($auth.isAuthenticated()).toEqual(false);
        });

    });
});
