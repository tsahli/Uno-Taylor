'use strict';

const Levels = require('./log_level.js');
const logger = new (require('./logging.js')).Logger('IPC');

const config = require('./config.js').load('application');

const net = require('net');
const rl = require('readline');

let pipeAddress = config.pipe_path || '/tmp/server.pipe';
logger.log(Levels.DEBUG, 'Pipe Address from config is ' + pipeAddress);

/*if (process.platform == 'win32') {
    logger.log(Levels.DEBUG, 'Pipe Address being changed for Windows');
    pipeAddress = '\\\\.\\pipe\\' + (pipeAddress.indexOf('/') == 0 ? pipeAddress.substr(1) : pipeAddress);
    pipeAddress = pipeAddress.replace(/\//g, '_');
    
    logger.log(Levels.DEBUG, 'New Pipe Address is ' + pipeAddress);
}*/

logger.log(Levels.DEBUG, 'Creating new server for queue');
const server = net.createServer();

const readers = [];
server.on('connection', function(pipe) {
    pipe.setEncoding('utf8');
    
    let reader = rl.createInterface({input: pipe});
    logger.log(Levels.FINE, 'Got new readline stream');
    
    pipe.on('error', function(error) {
        if (error.code != 'ECONNRESET') {
            logger.log(Levels.ERROR, 'Error on pipe, ' + error);
        }

        reader.close();
        pipe.end();

        let removeIndex = -1;
        readers.forEach(function(toRemove, index) {
            if (toRemove == reader) {
                removeIndex = index;
            }
        });

        if (removeIndex >= 0) {
            readers.splice(removeIndex, 1);
        }
    });
    
    reader.on('line', function(line) {
        logger.log(Levels.FINER, 'Reading line ' + line + ' from message queue');
        
        let json;
        try {
            json = JSON.parse(line);
        } catch (error) {
            logger.log(Levels.FINE, 'Message ' + line + ' was invalid JSON');
            return;
        }
        
        if (json.type == 'admin' && json.message == 'shutdown' && reader.isAdmin) {
            logger.log(Levels.INFO, 'Valid server shutdown request received');
            shutdown();
        }
        else if (json.type == 'password' && config.ipc_password != null && json.message == config.ipc_password) {
            logger.log(Levels.DEBUG, 'Admin module setup');
            reader.isAdmin = true;
        }
        else {
            broadcast(line);
        }
    });
    
    readers.push(reader);
});
logger.log(Levels.DEBUG, 'Attaching to pipe address');
server.listen(pipeAddress, function(err) {
    if (err != null) {
        logger.log(Levels.WARNING, 'Error listening to pipe address');
    }
    else {
        logger.log(Levels.INFO, 'Listening on pipe address ' + pipeAddress);
    }
});

function broadcast(message) {
    readers.forEach(function(reader) {
        reader.input.write(message + '\n');
    });
}

/**
 * Gets a pipe to the message queue. Note that it's the owning module's job to close it gracefully
 */
function getPipe() {
    let pipe = net.createConnection(pipeAddress);
    pipe.setEncoding('utf8');
    
    return pipe;
}

function shutdown() {
    broadcast('shutdown\n');
    server.close();
    
    let openCounter = 0;
    readers.forEach(function(reader, index, array) {
        openCounter++;
        reader.close();

        reader.input.on('close', function() {
            openCounter--;
        });
        reader.input.end();
        
        array[index] = null;
    });
    readers.length = 0;

    let intervalPtr = setInterval(function() {
        if (openCounter <= 0) {
            clearInterval(intervalPtr);
            process.exit(0);
        }
    }, 500);
}

module.exports.getPipe = getPipe;
