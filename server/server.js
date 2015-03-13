/**
 * @author: Michał 'salceson' Ciołczyk
 * @version: 0.0.1
 **/

var config = require("./config.json"),
    io = require("socket.io")(config.port);

var users = [];

console.log("Server started");

io.on('connection', function(socket){
    username = "";
    socket.emit("troll", {troll: true});
    socket.on('join', function(data){
        username = data;
        console.log(username);
    });
    //socket.on
});