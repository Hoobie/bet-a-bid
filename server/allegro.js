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
            cb(result.access_token);
        }
    });
}

function getAuctions(cb) {
    getToken(function (token) {
        rest.get(config.apis.allegro.endpoint + "v1/allegro/bargains", {
            query: {
                access_token: token,
                limit: 5,
                dailyOffers: false
            }
        }).on('complete', function (data, result) {
            if (result instanceof Error) {
                console.log('Error:', result.message);
                this.retry(2000); // try again after 2 sec
            } else {
                var to_send = {auctions: []};
                for (var i=0; i < data.bargains.length; i++){
                    var a = data.bargains[i];
                    to_send.auctions[i] = {
                        title: a.name,
                        image: a.image.large
                    };
                }
                cb(to_send);
            }
        });
    })
}

module.exports.getAuctions = getAuctions;

//getAuctions(function (a) {
//    console.log(a);
//});