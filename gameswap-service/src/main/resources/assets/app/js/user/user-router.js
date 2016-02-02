'use strict';

angular.module('gameswap')
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('users', {
                templateUrl: 'views/user/users.html',
                controller: 'UserController',
                resolve: {
                    resolvedUser: ['User', function (User) {
                        return User.query();
                    }]
                }
            })
    }]);
