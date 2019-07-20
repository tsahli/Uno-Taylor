'use strict';

const Levels = require('./log_level');
const config = require('./config.js').load('log');
const fs = require('fs');

let logger = null;

/**
 * Initializes a new logger for <code>moduleName</code>. This should only be required
 * for core modules. If not in a core module, the logger will be managed by the <code>LogManager</code>
 * initialized by the IPC module. Any module that creates it's own instance should listen for the
 * "shutdown" event from the logger before completing it's own shutdown.
 * Once instance of the logger can be initialized without a moduleName (this will be used to
 * log for the logger)
 * @param string - the name of the module this is being created for
 */
function Logger(moduleName) {
    if (moduleName == null && logger != null) {
        return logger;
    }
    
    const name = moduleName != null ? moduleName.replace(/ /g, '_') : 'Logger';
    
    let logLevel = config.level != null ? Levels.getLevel(config.level) : Levels.INFO;
    
    const streams = [];
    if (config.to_console || config.no_file) {
        streams.push(process.stdout);
    }
    
    let fileNames = [];
    if (config.no_file) {
        // No file streams to be set up
        if (logger != null) {
            logger.log(Levels.DEBUG, 'No files will be configured for ' + name + ' - see console', true);
        } 
    }
    // If no file names are in the config file, we have a default logged file (module name)
    else if (config.file_names == null) {
        fileNames.push(name);
        if (logger != null) {
            logger.log(Levels.DEBUG, 'Using default file for ' + name, true);
        }
    }
    // Otherwise, all file names (comma separated) will be added as a file
    else {
        fileNames = config.file_names.split(',');
        fileNames.forEach(function(fileName, index, array) {
            array[index] = name + '_' + fileName;
        });
    }
    
    // Set up the dir and flag so we don't do the same things over and over in the for loop
    let dir = config.dir == null ? '' : config.dir + '/';
    let writeFlag = config.clear_logs ? 'w' : 'a';
    fileNames.forEach(function(name) {
        let fileName = dir + name + '.log';
        let stream;
        try {
            let file = fs.openSync(fileName, writeFlag);
            stream = fs.createWriteStream(null, {fd: file});
        } catch (error) {
            // Just don't set up this stream (may be an invalid dir)
            logger.log(Levels.WARNING, 'Unable to set up ' + fileName + ', ' + error, true);
        }
        
        if (logger != null) {
            logger.log(Levels.DEBUG, 'Set up stream for ' + name + '.log', true);
        }
        streams.push(stream);
    });
    
    /**
     * Changes the loggers level
     * @param string OR int - must be a valid Levels type
     */
    this.changeLogLevel = function(newLevel) {
        if (Levels.isValidLevel(newLevel)) {
            logLevel = Levels.getLevel(newLevel);
            logger.log(Levels.DEBUG, 'Changing level for ' + name + ' to ' + Levels.getLevelName(logLevel), true);
        }
    };
    
    /**
     * This is used to log a message if <code>level</code> is greater than the Logger <code>logLevel</code>
     * @param int OR string - the <code>Level</code> at which to log this message
     * @param string - the message to log
     */
    this.log = function(level, message, noLog) {
        if (Levels.getLevel(level) >= logLevel) {
            if (!noLog) {
                logger.log(Levels.DEBUG, 'Logging ' + message + ' for ' + name, true);
            }
            let toWrite = Levels.getLevelName(level) + (name != null ? ' - ' + name : '') +
                            ': ' + message + '\n';
            streams.forEach(function(stream) {
                writeToStream(toWrite, stream, noLog);
            });
        }
        else if (!noLog) {
            logger.log(Levels.FINE, 'Not logging ' + message + ' for ' + name +
                    ' because ' + Levels.getLevelName(level) + ' is lower priority than ' +
                    Levels.getLevelName(logLevel),
                true);
        }
    };
    
    function writeToStream(message, stream, noLog) {
        if (!noLog) {
            logger.log(Levels.FINER, 'Attempt to write to ' + stream.fd, true);
        }
        if (!stream.write(message)) {
            if (!noLog) {
                logger.log(Levels.FINER, 'Stream not ready, waiting: ' + stream.fd);
            }
            stream.isWriting = true;
            if (stream.listeners('drain').length == 0) {
                if (!noLog) {
                    logger.log(Levels.FINER, 'Stream is ready again: ' + stream.fd, true);
                }
                stream.once('drain', function() { stream.isWriting = false; });
            }
        }
    }
    
    /**
     * Closes all streams for this logger (except stdout)
     */
    this.shutdown = function() {
        logger.log(Levels.DEBUG, 'Shutting down logger ' + name, true);
        streams.forEach(function(stream, index, array) {
            if (stream != process.stdout) {
                while(stream.isWriting) {
                    // Wait for the file to finish writing
                }
                stream.end();
            }
            
            array[index] = null;
        });
        
        streams.length = 0;
        
        logger.log(Levels.DEBUG, 'Logger ' + name + ' shut down', true);
    };
    
    return this;
}

logger = new Logger();

module.exports.Logger = Logger;
