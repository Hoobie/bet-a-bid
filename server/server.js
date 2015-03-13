/**
 * @author: Michał 'salceson' Ciołczyk
 * @version: 0.0.1
 **/

var config = require("./config.json"),
    io = require("socket.io")(config.port);

console.log("Bangla");