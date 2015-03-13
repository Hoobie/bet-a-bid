/**
 * @author: Michał 'salceson' Ciołczyk
 * @version: 0.0.1
 **/

var config = require("./config.json"),
    io = require("socket.io")(config.port);

var users = [];

var state = 0;

console.log("Server started");

io.on('connection', function (socket) {
    var username = "";
    socket.emit("troll", {troll: true});
    socket.on("join", function (data) {
        username = data;
        console.log(username);
        if (state == 0) {
            users[0] = {username: username, socket: socket};
            socket.emit("user", 0);
            state = 1;
        } else if (state == 1) {
            users[1] = {username: username, socket: socket};
            socket.emit("user", 1);
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
        users[data.user].results = data.result;
        if(state == 3)
            state = 4;
        else {
            state = 5;
            var match = ~(users[0].results ^ users[1].results);
            for(var i=0; i<2; i++){
                users[i].socket.emit("matches", true);
            }
        }
    });
});

function downloadAuctions(){
    var auctions = {};
    for(var i=0; i<2; i++){
        users[i].socket.emit("auctions", auctions);
    }
}