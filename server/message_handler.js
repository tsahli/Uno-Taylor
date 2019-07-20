'use strict';

const fs = require('fs');
const spawn = require('child_process').spawn;

const config = require('./config.js').load('application');
const adminUsers = config.admin_users.split(',');
adminUsers.forEach(function(user, index, array) {
    let thisUser = user.split(':');
    thisUser = {name: thisUser[0], password: thisUser[1]};
    
    array[index] = thisUser;
});
const moduleAdmins = [];

const Levels = require('./log_level');
const Logger = require('./logging.js');
const logger = new Logger.Logger('Message Handler');

const Types = require('./type');

const rl = require('readline');
const reader = rl.createInterface({input: require('./ipc.js').getPipe()});
sendMessage({type: 'password', message: 'ipcpass'}, reader);
reader.on('line', function(line) {
    if (line == 'shutdown') {
        shutdown();
    }

    let json;
    try {
        json = JSON.parse(line);
    } catch (error) {
        // Don't do anything else
        return;
    }

    if (json.action == 'broadcast') {
        if (json.module != null) {
            moduleBroadcast({type: Types.APP, message: json.message}, json.module, json.username);
        }
        else {
            broadcast({type: Types.APP, message: json.message});
        }
    }
    if (json.action == 'netSend') {
        users.forEach(function(user) {
            if (user.username == json.username) {
                sendMessage(json.message, user);
            }
        });
    }
});

const modules = [];
// This is used to allow us to upload multiple modules at once
const newModules = {};
const started = [];
const users = [];
const moduleUsers = {};
function handle(socket) {
    let user = rl.createInterface({input: socket});
    user.on('line', function(line) {
        let json;
        try {
            json = JSON.parse(line);
        } catch (error) {
            logger.log(Levels.DEBUG, 'Invalid JSON' + (user.username != null ? ' from ' + user.username : ''));
            return;
        }
        
        if (json.type == Types.LOGIN) {
            let message = json.message;
            if (message == null || message.username == null) {
                logger.log(Levels.DEBUG, 'No username for login message');
                sendMessage({type: Types.ERROR, message: 'Username is required for login messages'}, user);
                return;
            }
            
            let admin = adminUsers.find(function(adminUser) {
                return adminUser.name == message.username;
            });
            
            if (admin != null) {
                logger.log(Levels.INFO, 'Got admin user login message');
                if (message.password != admin.password) {
                    logger.log(Levels.INFO, 'Invalid password for user ' + message.username);
                    sendMessage({type: Types.ERROR, message: 'Password sent was not correct for user ' + message.username}, user);
                    return;
                }
            }
            
            let loggedIn = users.find(function(loggedInUser) {
                return loggedInUser.username == message.username;
            });
            
            if (loggedIn != null) {
                logger.log(Levels.INFO, 'Got attempted login as ' + message.username + ' who is already logged in');
                sendMessage({type: Types.ERROR, message: 'Username ' + message.username + ' is already logged in'}, user);
                return;
            }
            
            logger.log(Levels.INFO, 'Logging in ' + message.username);
            user.username = message.username;
            sendMessage({type: Types.ACK, message: message.username + ' successfully logged in'}, user);
            if (admin != null) {
                logger.log(Levels.INFO, message.username + ' is logged in as administrator');
                user.isAdmin = true;
            }
            else if (moduleAdmins.indexOf(user.username) >= 0) {
                logger.log(Levels.INFO, message.username + ' is logged in as module administrator');
                user.isModuleAdmin = true;
            }

            return;
        }

        if (user.username == null) {
            return;
        }
        else if (json.type == Types.ADMIN) {
            if (!user.isAdmin && !user.isModuleAdmin) {
                logger.log(Levels.INFO, 'Got admin message from non-admin user');
                sendMessage({type: Types.ERROR, message: 'User is not an authorized admin'}, user);
                return;
            }
            
            if (user.isAdmin && json.message == 'shutdown') {
                sendMessage({type: 'admin', message: 'shutdown'}, reader);
            }
            else if (user.isAdmin && json.message == 'add_module_admin' && moduleAdmins.indexOf(json.name) < 0) {
                moduleAdmins.push(json.name);
            }
            else if (user.isAdmin && json.message == 'remove_user' && json.username != null) {
                log(Types.INFO, 'Removing user ' + json.username);
                users.forEach(function(potentialRemoval, index, array) {
                    if (potentialRemoval.username == json.username) {
                        log(Types.INFO, 'Found user named ' + json.username);
                        user.close();
                        user.input.end();

                        array[index] = null;
                    }
                });
            }
            else if (json.message == 'scan') {
                sendMessage({type: Types.ADMIN, message: 'Scanning for modules'}, user);
                logger.log(Levels.INFO, 'Starting scan for new modules');

                updateModules(user, function() {
                    logger.log(Levels.INFO, 'Current modules: ' + modules.join(', '));
                    sendMessage({type: Types.ADMIN, message: 'Current modules: ' + modules.join(', ')}, user);
                });
            }
            else if (json.message == 'upload') {
                // This will create the modules directory if it's not there and give us a list of modules
                updateModules(user, function() {
                    if (newModules[json.name] == null) {
                        logger.log(Levels.INFO, 'Beginning upload of file ' + json.name);
                        newModules[json.name] = {};
                    }

                    logger.log(Levels.DEBUG, 'Entering part ' + json.part + ' of ' + json.name);
                    newModules[json.name]['part' + json.part] = json.contents;

                    if (json.lastPart) {
                        logger.log(Levels.DEBUG, json.name + ' is ' + json.lastPart + ' entries');
                        newModules[json.name].lastPartNumber = json.part;
                    }

                    if (newModules[json.name].lastPartNumber == null) {
                        logger.log(Levels.DEBUG, json.name + ' waiting for last part');
                        return;
                    }

                    let fileString = '';
                    for (let i = 1; i <= newModules[json.name].lastPartNumber; i++) {
                        if (newModules[json.name]['part' + i] == null) {
                            logger.log(Levels.DEBUG, json.name + ' still has parts remaining');
                            return;
                        }

                        fileString += newModules[json.name]['part' + i];
                    }

                    let fileName = json.name.split('.');
                    fileName = fileName.splice(0, fileName.length - 1).join('.');
                    fs.unlink('modules/' + fileName + '.js', function(err) { /* we expect this to happen, ignore */ });
                    fs.unlink('modules/' + fileName + '.jar', function(err) { /* we expect this to happen, ignore */ });

                    logger.log(Levels.INFO, 'Writing to modules/' + json.name);
                    fs.writeFile('modules/' + json.name, fileString, 'base64', function(error) {
                        if (error) {
                            logger.log(Levels.WARNING, 'Unable to write modules/' + json.name + ', ' + error);
                            sendMessage(
                                    {type: Types.ERROR,
                                        message: 'Unable to write ' + json.name + ', see logs for details'},
                                    user);
                        }
                    });
                });
            }
            else if (json.message == 'start') {
                if (modules.indexOf(json.name) < 0) {
                    logger.log(Levels.WARNING, 'Unable to start unknown module ' + json.name);
                    sendMessage({type: Types.ERROR, message: 'Module ' + json.name + ' not found'}, user);
                    return;
                }

                if (started.indexOf(json.name) >= 0) {
                    logger.log(Levels.DEBUG, 'Not starting already running module ' + json.name);
                    sendMessage({type: Types.ERROR, message: 'Module ' + json.name + ' already started'}, user);
                    return;
                }

                fs.readdir('modules', function(err, files) {
                    let processed = false;
                    files.forEach(function(file) {
                        let fileName = file.split('.');
                        let ext = fileName.splice(fileName.length - 1);
                        fileName = fileName.join('.');

                        if ((ext != 'js' && ext != 'jar') || fileName != json.name || processed) {
                            return;
                        }

                        processed = true;

                        let command = ext == 'js' ? 'node' : 'java';
                        let options = ['./modules/' + fileName + '.' + ext, config.pipe_path];
                        if (command == 'java') {
                            options.unshift('-jar');
                        }

                        let child = spawn(command, options);
                        child.on('error', function(error) {
                            logger.log(Levels.WARNING, error + '');
                            sendMessage({type: Types.ERROR, message: 'Module ' + json.name + ' started with an error, module shutdown'}, user);

                            clearChild(json.name);
                        });
                        child.on('exit', function(code, signal) {
                            if (code == null) {
                                logger.log(Levels.WARNING, 'Module ' + json.name + ' exited with signal ' + signal, + ', removing from modules');
                            }

                            clearChild(json.name);
                        });

                        logger.log(Levels.INFO, 'Started module ' + json.name + ' as separate process');
                        started.push(json.name);
                    });
                });
            }
            else if (json.message == 'view_log') {
                fs.readdir('log', function(err, files) {
                    let logExists = false;
                    let hasUnderscore = false;

                    logger.log(Levels.DEBUG, 'Looking for log file ' + json.name + ' w/ or w/out underscore');
                    files.forEach(function(file) {
                        if (json.name + '.log' == file || json.name + '_.log' == file) {
                            logExists = true;
                            if (file.substr(json.name.length, 1) == '_') {
                                hasUnderscore = true;
                            }
                        }
                    });

                    if (!logExists) {
                        return;
                    }

                    logger.log(Levels.DEBUG, 'Reading log file for ' + json.name);
                    let lineNumber = 0;
                    let logReader = rl.createInterface(
                        {input: fs.createReadStream('log/' + json.name + (hasUnderscore ? '_' : '') + '.log')
                    });

                    logReader.on('line', function(line) {
                        let logMessage = {type: 'log_read',
                                            message: line + '',
                                            line_number: lineNumber++};
                        sendMessage(logMessage, user);
                    });
                });
            }
        }
        else if (json.type == Types.APP && json.message != null && typeof json.message == "object") {
            if (user.username == null) {
                return;
            }
            addModuleUser(user, json.message.module);

            json.message.username = user.username;
            sendMessage(json.message, reader);
        }
        else if (json.type == Types.CHAT) {
            if (typeof json.message != 'string') {
                logger.log(Levels.WARNING, 'Attempting to "chat" a non-text message');
                sendMessage({type: Types.ERROR, message: 'Unable to send non-text chat messages, correct your message and send again'}, user);
                return;
            }

            if (json.username == null) {
                broadcast(json, user.username);
            }
            else {
                users.forEach(function(toSend) {
                    if (toSend.username == json.username) {
                        json.fromUser = user.username;

                        sendMessage(json, toSend);
                    }
                });
            }
        }
        else if (json.type == Types.WHOIS) {
            let whois = {type: Types.WHOIS};
            let whoisMessage = {};
            whoisMessage.users = [];
            
            users.forEach(function(networkUser) {
                if (networkUser.username != null) {
                    whoisMessage.users.push({username: networkUser.username, modules: []});
                }
            });

            for (var mod in moduleUsers) {
                if (!moduleUsers.hasOwnProperty(mod)) {
                    continue;
                }

                moduleUsers[mod].forEach(function(moduleUser) {
                    whoisMessage.users.forEach(function(whoisUser) {
                        if (moduleUser.username == whoisUser.username) {
                            whoisUser.modules.push(mod);
                        }
                    });
                });
            }

            whoisMessage.modules = [];
            modules.forEach(function(mod) {
                let messageModule = {moduleName: mod, started: false};

                started.forEach(function(name) {
                    if (name == mod) {
                        messageModule.started = true;
                    }
                });

                whoisMessage.modules.push(messageModule);
            });

            whois.message = whoisMessage;
            sendMessage(whois, user);
        }
    });

    socket.on('error', function(error) {
        if (error.code == 'ECONNRESET') {
            logger.log(Levels.FINE, 'Connection ended by other party');
        }
        else {
            logger.log(Levels.DEBUG, 'Error on user socket, ' + error);
        }

        removeUser(user, socket);
    });
    socket.on('close', function() {
        removeUser(user, socket);
    });
    
    users.push(user);
}

function removeUser(user, socket) {
	if (user.username != null) {
		for (var module in moduleUsers) {
			if (!moduleUsers.hasOwnProperty(module)) {
				continue;
			}
			
			moduleUsers[module].forEach(function(user, index, users) {
                sendMessage({module: module, action: 'quit', username: user.username}, reader);
				users.splice(index--, 1);
			});
		}
	}
    user.close();
    socket.end();
    
    let socketIndex = null;
    users.forEach(function(toRemove, index) {
        if (toRemove == user) {
            socketIndex = index;
        }
    });

    if (socketIndex != null) {
        users.splice(socketIndex, 1);
    }
}

function updateModules(user, callback) {
    fs.readdir('modules', function(err, files) {
        if (err != null) {
            // Modules directory hasn't been initialized yet
            if (err.code == 'ENOENT') {
                fs.mkdir('modules', function(err) {
                    if (err != null) {
                        logger.log(Levels.ERROR, 'Couldn\'t create directory modules');
                    }
                });
            }
            logger.log(Levels.ERROR, 'Error while scanning for modules: ' + err);
            sendMessage({type: Types.ADMIN, message: 'Error while scanning, see log for details'}, user);
            return;
        }

        files.forEach(function(file) {
            let fileName = file.split('.');
            let extension = fileName.splice(fileName.length - 1);
            fileName = fileName.join('.');

            if (extension != 'js' && extension != 'jar') {
                logger.log(Levels.FINE, 'File is not a module file (' + file + ')');
                return;
            }

            if (modules.indexOf(fileName) < 0) {
                logger.log(Levels.FINER, 'Found ' + file + ', adding to modules');
                modules.push(fileName);
            }
        });

        for (let i = 0; i < modules.length; i++) {
            let java = modules[i] + '.jar';
            let js = modules[i] + '.js';
            if (files.indexOf(java) < 0 && files.indexOf(js) < 0) {
                modules.splice(i--, 1);
            }
        }

        callback();
    });
}

function clearChild(childName) {
    let foundIndex = -1;
    started.forEach(function(startedModule, index) {
        if (startedModule == childName) {
            foundIndex = index;
        }
    });

    if (foundIndex >= 0) {
        started.splice(foundIndex, 1);
    }
}

function shutdown() {
    broadcast({type: Types.ADMIN, message: 'shutdown'});
    
    users.forEach(function(user, index, array) {
        user.close();
        user.input.end();
        
        array[index] = null;
    });
    
    users.length = 0;
    
    logger.log(Levels.INFO, 'Message handler shut down, closing IPC');
    reader.close();
    reader.input.end();

    setTimeout(function() { process.exit(0); }, 500);
}

function sendMessage(message, user, toUser) {
    let json = JSON.stringify(message);
    logger.log(Levels.FINE, 'Sending message ' + json + (user.username != null ? ' to ' + user.username : ''));
    user.input.write(json + '\n');
}

function broadcast(message, username) {
    logger.log(Levels.DEBUG, 'Broadcasting ' + message);
    if (username != null) {
        message.fromUser = username;
    }
    
    users.forEach(function(user) {
        if (user.username != null && (username == null || user.username != username)) {
            sendMessage(message, user);
        }
    });
}

function moduleBroadcast(message, moduleName, username) {
    logger.log(Levels.DEBUG, 'Broadcasting ' + message + ' to module ' + moduleName);
    if (username != null) {
        message.fromUser = username;
    }

    let mod = moduleUsers[moduleName] || [];
    mod.forEach(function(user) {
        if (username != null && user.username == username) {
            return;
        }

        sendMessage(message, user);
    });
}

function addModuleUser(user, moduleName) {
    let mod = moduleUsers[moduleName];
    if (mod == null) {
        mod = [];
        moduleUsers[moduleName] = mod;
    }

    if (mod.indexOf(user) < 0) {
        for (var i = 0, j = mod.length; i < j; i++) {
	    if (mod[i] != null && mod[i].username == user.username && mod[i].closed) {
	        mod.splice(i, 1);
	    }
	}

        mod.push(user);
    }
}

module.exports.handle = handle;
