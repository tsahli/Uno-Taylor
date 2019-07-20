'use strict';

module.exports.load = function(configName) {
    let reader;
    const config = {};
    
    try {
        reader = require('fs').readFileSync(configName + '.config', 'utf8');
        reader = reader.split(/[\r]?\n/);
    } catch (error) {
        // This means we can't load, return the empty config
        return config;
    }
    
    // take every non-comment (# at beginning) line and put it into the config object
    reader.forEach(function(line) {
        if (line.indexOf("#") == 0) {
            return;
        }
        
        line = line.split("=");
        if (line.length > 2) {
            line[1] = line.splice(1).join("=");
        }
        else if (line.length < 2) {
            return;
        }
        
        config[line[0]] = line[1];
    });
    
    // Convert parameters that should be true/false to boolean and integer to number instead of string values
    for (var name in config) {
        if (config.hasOwnProperty(name)) {
            config[name] = config[name] == 'false' ? false : (config[name] == 'true' ? true : config[name]);
            config[name] = isNaN(parseInt(config[name], 10)) ? config[name] : parseInt(config[name], 10);
        }
    }
    
    return config;
};
