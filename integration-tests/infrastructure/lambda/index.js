'use strict';

module.exports.handler = async () => ({
    statusCode: 200,
    headers: {
        'Content-Type': 'text/plain'
    },
    body: 'Hello'
});
