describe('LoginCtrl', function () {
    var $httpBackend, $rootScope, controller, $auth, $location;
    var loginUrl = '/gameswap/auth/login';

    // Set up the module
    beforeEach(module('gameswap'));

    beforeEach(inject(function ($injector) {
        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');
        //// Get hold of a scope (i.e. the root scope)
        $rootScope = $injector.get('$rootScope');
        console.log("LOGGING");
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


    describe('login()', function() {
        it('successful login should fetch authentication token', function () {
            $httpBackend.when('POST', loginUrl).respond(200, {token: 'somelongtoken'});
            $httpBackend.expectPOST(loginUrl);
            $rootScope.login();
            $httpBackend.flush();
            expect($auth.isAuthenticated()).toEqual(true);
        });


        it('unsuccessful login, user is not authenticated', function () {
            //// Notice how you can change the response even after it was set
            $rootScope.login();
            $httpBackend.when('POST', loginUrl).respond(401, {"code": 500, "message": "Wrong email and/or password"});
            $httpBackend.flush();
            expect($auth.isAuthenticated()).toEqual(false);
        });

    });
});
