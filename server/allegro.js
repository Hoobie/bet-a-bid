var rest = require('restler');

var config = require("./config.json");

function getToken(cb) {
    rest.get(config.apis.allegro.oauth, {
        username: config.apis.allegro.userLogin,
        password: config.apis.allegro.pass
    }).on('complete', function (result) {
        if (result instanceof Error) {
            console.log('Error:', result.message);
            this.retry(2000); // try again after 2 sec
        } else {
            console.log(result);
            cb(result.access_token);
        }
    });
}

function getAuctions(cb) {
    getToken(function (token) {
        console.log(token);
        rest.get(config.apis.allegro.endpoint + "v1/allegro/bargains", {
            query: {
                access_token: token,
                limit: 3,
                dailyOffers: true
            }
        }).on('complete', function (data, result) {
            if (result instanceof Error) {
                console.log('Error:', result.message);
                this.retry(2000); // try again after 2 sec
            } else {
                console.log(data);
                cb();
            }
        });
    })
}

getAuctions(function () {
});