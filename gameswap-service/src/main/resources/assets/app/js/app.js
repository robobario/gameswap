// Declare app level module which depends on filters, and services
angular.module('gameswap', ['ngMessages', 'restangular', 'ui.router', 'satellizer', 'ngAnimate', 'toastr' ])

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
                }).state('profile', {
                    url: '/profile',
                    templateUrl: 'views/partials/profile.html',
                    controller: 'ProfileCtrl',
                    resolve: {
                        loginRequired: loginRequired
                    }
                })
                .state('logout', {
                    url: '/logout',
                    template: null,
                    controller: 'LogoutCtrl'
                });

            $urlRouterProvider.otherwise('/');
            $authProvider.baseUrl = "/gameswap";
            $authProvider.google({
                clientId: '437170734308-t3519t3mtgl8p5ipqdi4jpns0r5pa36g.apps.googleusercontent.com'
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

            function loginRequired($q, $auth) {
                var deferred = $q.defer();
                if ($auth.isAuthenticated()) {
                    deferred.resolve();
                } else {
                    $state.go('login');
                }
                return deferred.promise;
            }
        }]);
