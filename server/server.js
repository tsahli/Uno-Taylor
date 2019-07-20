'use strict';

const config = require('./config.js').load('application');
const port = config.port || 8989;

const messageHandler = require('./message_handler.js');

const Levels = require('./log_level.js');
const Logger = require('./logging.js');
const logger = new Logger.Logger('Server');

logger.log(Levels.INFO, 'Setting up log manager');
require('./log_manager.js');

logger.log(Levels.INFO, 'Creating network server');
const server = require('net').createServer();

server.on('connection', function(socket) {
    logger.log(Levels.DEBUG, 'Got a connection, moving to the message handler');
    messageHandler.handle(socket);
});

server.listen(port, function(err) {
    if (err != null) {
        logger.log(Levels.ERROR, 'Trying to listen on port ' + port + ', encountered ' + err);
    }
    else {
        logger.log(Levels.INFO, 'Listening on port ' + port);
    }
});

// The reason we get the reader here is to be able to do things like reset the server
// Most other admin tasks will be handled by the Message Handler and individual game admin
// tasks will be handled by those games (hopefully - no promises, but it's in the contract)
logger.log(Levels.INFO, 'Getting IPC pipe');
const reader = require('readline').createInterface({input: require('./ipc.js').getPipe()});
reader.on('line', function(line) {
    if (line == 'shutdown') {
        shutdown();
    }
});

function shutdown() {
    logger.log(Levels.INFO, 'Shutting down server');
    server.on('close', function() {
        logger.log(Levels.INFO, 'Shutdown complete, exiting');
        
        setTimeout(function() {
            process.exit(0);
        }, 2);
    });
    
    server.close();
}
