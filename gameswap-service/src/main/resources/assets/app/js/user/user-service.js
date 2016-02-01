'use strict';

angular.module('gameswap')
  .factory('User', ['$resource', function ($resource) {
    return $resource('gameswap/users/:id', {}, {
      'query': { method: 'GET', isArray: true},
      'get': { method: 'GET'},
      'update': { method: 'PUT'}
    });
  }]);
