describe('ProfileCtrl', function () {
    var $httpBackend, $rootScope, controller, restangular;
    var testUsersUrl = '/gameswap/profile/1';

    var mock = {
        getPayload: function () {
            return {
                sub: "1"
            }
        },
        isAuthenticated: function() {
            return true;
        }
    };

    // Set up the module
    beforeEach(module('gameswap'));

    beforeEach(inject(function ($injector) {
        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');
        //// Get hold of a scope (i.e. the root scope)
        $rootScope = $injector.get('$rootScope');

        restangular = $injector.get('Restangular');
        //// The $controller service is used to create instances of controllers
        var $controller = $injector.get('$controller');
        var toastr = $injector.get('toastr');

        controller = $controller('ProfileCtrl', {
            '$scope': $rootScope,
            '$auth': mock,
            'Restangular': restangular,
            '$state': mock
        });
    }));


    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });


    describe('get user', function () {
        it('retrieve known user data', function () {
            $httpBackend.when('GET', testUsersUrl).respond({
                displayName: "Ted"
            });
            $httpBackend.expectGET(testUsersUrl);
            $httpBackend.flush();
            expect($rootScope.user.displayName).toEqual("Ted");
        });
    });
});

