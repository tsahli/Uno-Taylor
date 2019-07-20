'use strict';

const Levels = require('./log_level.js');
const Logger = require('./logging.js');

const logger = new Logger.Logger('LogManager');
    
const loggers = [];
    
const reader = require('readline').createInterface({input: require('./ipc.js').getPipe()});
reader.on('line', function(line) {
    if (line == 'shutdown') {
        shutdown();
        return;
    }
    
    let json;
    try {
        json = JSON.parse(line);
    } catch (error) {
        logger.log(Levels.DEBUG, 'Invalid json found: ' + line);
        return;
    }
    
    if (json.action == 'log') {
        logMessage(json);
    }
    else if (json.action == 'update_log_level') {
        logger.log(Levels.DEBUG, 'Attempting level update');

        let currentLogger = getLogger(json.module);
        
        if (Levels.isValidLevel(json.level)) {
            logger.log(Levels.INFO, 'Updating ' + json.module + ' level to ' + Levels.getLevelName(json.level));
            currentLogger.changeLogLevel(json.level);
        }
    }
});

function logMessage(json) {
    if (json.module == null) {
        logger.log(Levels.DEBUG, 'Null module name received');
        return;
    }
    if (!Levels.isValidLevel(json.level)) {
        logger.log(Levels.DEBUG, 'Bad level received for module ' + json.module + ': ' + json.level);
        return;
    }
    if (json.text == null || typeof json.text != 'string') {
        logger.log(Levels.DEBUG, 'Invalid message received for module ' + json.module + ': ' + json.text);
        return;
    }
    
    let currentLogger = getLogger(json.module);

    logger.log(Levels.DEBUG, 'Logging ' + json.text + ' at level ' + json.level);
    currentLogger.log(json.level, json.text);
}

function getLogger(moduleName) {
    let currentLogger = loggers.find(function(logger) {
        return logger.module == moduleName;
    });
    
    if (currentLogger == null) {
        logger.log(Levels.INFO, 'Setting up a new logger for ' + moduleName);
        currentLogger = new Logger.Logger(moduleName);
        currentLogger.module = moduleName;
        loggers.push(currentLogger);
    }
    
    return currentLogger;
}

function shutdown() {
    logger.log(Levels.INFO, 'Shutting down');
    loggers.forEach(function(logger, index, array) {
        logger.shutdown();
        
        array[index] = null;
    });
    
    logger.log(Levels.DEBUG, 'Ending LogManager logger');
    loggers.length = 0;
    logger.shutdown();
    
    logger.log(Levels.DEBUG, 'Closing line reader');
    reader.close();
    logger.log(Levels.DEBUG, 'Ending pipe');
    reader.input.end();
}
