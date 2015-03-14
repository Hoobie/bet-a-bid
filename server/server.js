/**
 * @author: Michał 'salceson' Ciołczyk
 * @version: 0.0.1
 **/

var config = require("./config.json"),
    io = require("socket.io")(config.port),
    allegro = require("./allegro.js");

var users = [], results = [], usernames = [], images = [], state = 0;
var imgs = ["http://www.dannyst.com/blogimg/5-sec-faces-photo-12.jpg",
            "http://defwalls.com/wallpapers-n/men-people-actors-Daniel-Craig-faces-_4819-38.jpg"];

console.log("Server started");

io.on('connection', function (socket) {
    var username = "";

    socket.on("join", function (data) {
        username = data.name || "Guest";
        var image = data.image || "";
        console.log(username + " joined the server");
        usernames.push(username);
        images.push(image);
        if (state == 0) {
            users.push(socket);
            state = 1;
            //downloadAuctions();
        } else if (state == 1) {
            users.push(socket);
            state = 2;
            downloadAuctions();
        } else {
            console.log("error");
            socket.close();
        }
    });

    socket.on("results", function (data) {
        if (state != 3 && state != 4) {
            console.log("error");
            socket.close();
            return;
        }
        results.push(data);
        if (state == 3)
            state = 4;
        else {
            var match = ~(results[0] ^ results[1]) & (Math.pow(2, config.auctions) - 1);
            for (var i = 0; i < users.length; i++) {
                //if(match > 0) {
                    users[i].emit("finish", {
                        matched: match > 0,
                        username: usernames[1-i],
                        //image: images[1-i]
                        image: imgs[1-i]
                    });
                //} else {
                //    users[i].emit("finish", {matched: false});
                //}
            }
            users = [];
            results = [];
            images = [];
            usernames = [];
            state = 0;
        }
    });

    socket.on('disconnect', function (data) {
        //socket.emit('disconnected');
        console.log(username + " disconnected");
        var i = users.indexOf(socket), j = 0;
        delete users[i];
        var tab = [];
        for (i = 0; i < users.length; i++) {
            if (users[i] === undefined) continue;
            tab[j++] = users[i];
        }
        users = tab;
        if (state > 1) {
            state = 1;
        } else {
            state = 0;
        }
    });
});

function downloadAuctions() {
    allegro.getAuctions(function (data) {
        for (var i = 0; i < users.length; i++) {
            users[i].emit("paired", data);
        }
        state = 3;
    });
}