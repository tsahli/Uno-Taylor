'use strict';

/**
 * The Levels object contains all the information we need to make the log levels work
 * This includes translating a name or integer value into a name or integer value
 */
const Levels = {};
Levels.array = ['FINEST', 'FINER', 'FINE', 'DEBUG', 'INFO', 'WARNING', 'ERROR'];
// This makes sure our array and values are synchronized
Levels.array.forEach(function(level, index) {
    Levels[level] = index;
});
/**
 * Checks to make sure the given level is a valid log level
 * @param string OR int - the number or name level value
 */
Levels.isValidLevel = function(level) {
    if ((typeof level != 'string' && typeof level != 'number') ||
            (Levels.array[level] == null && Levels[level] == null)) {
        return false;
    }
    
    return true;
};
/**
 * This gets the numeric level value if it exists, null otherwise
 * @param string OR int - the number or name level value
 */
Levels.getLevel = function(level) {
    if (Levels.isValidLevel(level)) {
        return Levels[level] == null ? level : Levels[level];
    }
    
    return null;
};
/**
 * This gets the level name if it exists (if the level passed is the name, it's simply returned)
 * @param string OR int - the number or name level value
 */
Levels.getLevelName = function(level) {
    if (Levels.isValidLevel(level)) {
        return Levels.array[Levels.getLevel(level)];
    }
    
    return null;
};

module.exports = Levels;
