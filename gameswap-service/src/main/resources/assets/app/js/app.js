// Declare app level module which depends on filters, and services
angular.module('gameswap', ['ngResource', 'ui.bootstrap', 'ui.date', 'ui.router', 'satellizer', 'ngAnimate', 'toastr'])

    .config(['$stateProvider', '$urlRouterProvider', '$authProvider',
        function ($stateProvider, $urlRouterProvider, $authProvider) {
            $stateProvider
                .state('home', {
                    url: '/',
                    controller: 'HomeController',
                    templateUrl: 'views/home/home.html'
                })
                .state('login', {
                    url: '/login',
                    templateUrl: 'views/partials/login.html',
                    controller: 'LoginCtrl',
                    resolve: {

                        skipIfLoggedIn: skipIfLoggedIn
                    }
                })
                .state('signup', {
                    url: '/signup',
                    templateUrl: 'views/partials/signup.html',
                    controller: 'SignupCtrl',
                    resolve: {
                        skipIfLoggedIn: skipIfLoggedIn
                    }
                })
                .state('logout', {
                    url: '/logout',
                    template: null,
                    controller: 'LogoutCtrl'
                })
                .state('profile', {
                    url: '/profile',
                    templateUrl: 'views/partials/profile.html',
                    controller: 'ProfileCtrl',
                    resolve: {
                        loginRequired: loginRequired
                    }
                });

            $urlRouterProvider.otherwise('/');

            $authProvider.google({
                clientId: 'Google Client ID'
            });

            function skipIfLoggedIn($q, $auth) {
                var deferred = $q.defer();
                if ($auth.isAuthenticated()) {
                    deferred.reject();
                } else {
                    deferred.resolve();
                }
                return deferred.promise;
            }

            function loginRequired($q, $location, $auth) {
                var deferred = $q.defer();
                if ($auth.isAuthenticated()) {
                    deferred.resolve();
                } else {
                    $location.path('/login');
                }
                return deferred.promise;
            }
        }]);
