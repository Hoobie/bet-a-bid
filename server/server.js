/**
 * @author: Michał 'salceson' Ciołczyk
 * @version: 0.0.1
 **/

var config = require("./config.json"),
    io = require("socket.io")(config.port),
    allegro = require("./allegro.js");

var users = [], results = [], state = 0;

console.log("Server started");

io.on('connection', function (socket) {
    var username = "";
    socket.emit("troll", {troll: true});
    socket.on("join", function (data) {
        username = data;
        console.log(username);
        if (state == 0) {
            users[0] = {username: username, socket: socket};
            state = 1;
        } else if (state == 1) {
            users[1] = {username: username, socket: socket};
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
        results.push(data.result);
        if (state == 3)
            state = 4;
        else {
            var match = ~(results[0] ^ results[1]) & 0x31;
            for (var i = 0; i < 2; i++) {
                users[i].socket.emit("matches", match > 0);
                users[i].socket.close();
            }
            users = [];
            results = [];
            state = 0;
        }
    });
});

function downloadAuctions() {
    allegro.getAuctions(function (data) {
        for (var i = 0; i < 2; i++) {
            users[i].socket.emit("paired", data);
        }
    });
}