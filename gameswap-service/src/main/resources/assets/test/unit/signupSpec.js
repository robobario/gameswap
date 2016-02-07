describe('SignupCtrl', function () {
    var $httpBackend, $rootScope, controller, $auth;
    var signupUrl = '/gameswap/auth/signup';

    // Set up the module
    beforeEach(module('gameswap'));

    beforeEach(inject(function ($injector) {
        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');
        //// backend definition common for all tests
        //
        //// Get hold of a scope (i.e. the root scope)
        $rootScope = $injector.get('$rootScope');
        $auth = $injector.get('$auth');
        //// The $controller service is used to create instances of controllers
        var $controller = $injector.get('$controller');
        var $location = $injector.get('$location');
        var toastr = $injector.get('toastr');

        controller = $controller('SignupCtrl', {
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

    describe('signup()', function () {
        it('successful signup attempt', function () {
            $httpBackend.when('POST', signupUrl)
                .respond(200, {"token": "eyJhbGciOiJIUzI1NiJ9"});
            $httpBackend.expectPOST(signupUrl);
            $rootScope.signup();
            $httpBackend.flush();
            expect($auth.isAuthenticated()).toEqual(true);
        });
    });
});
