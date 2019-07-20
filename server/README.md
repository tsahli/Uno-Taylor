# 3230 Final Server #

This server is built to allow easy module installation for individual game/program types
for the CS 3230 class at Weber State. There will be a JavaScript (NodeJS) and a Java version
of this server. Modules communicate via an IPC queue, so they can be built in any language.
(I may install a Python version of this server at some point, to demonstrate the point to the students)

## Logging ##

To send a log message on the server, you should register an IPC read/write stream, and send
a JSON message of type "log" with a "module" (always the same for your module), "message",
and "level". The log level can be anything in the ```Levels.array``` array.

### Config File ###

The ```log.config``` file is used to set global logging properties. This includes things like
whether the logger should go to the console or not, file names to be logged, and what directory
to log to. Below is a list of these options:

* ```level=``` The level from ```Levels.array```
* ```dir=``` The (probably relative) directory to put all log files into
* ```to_console=``` A boolean to indicate whether to log to the console
* ```clear_logs=``` A boolean indicating whether to append to the logs (false) or overwrite them (true)
* ```no_file=``` A boolean indicating that no files should be written, all loggers will write to stdout
* ```file_names=``` Comma separated file name suffixes for all modules (not often useful, my guess)

### Methods ###

The ```log``` method requires a level and a message, and outputs the message if the given level is greater
than the logger's currently set log level. This can be changed by using ```logger.changeLogLevel```. ```shutdown```
attempts to gracefully close all streams for the logger.

### IPC Logging ###

The logger can also be accessed by sending messages on the IPC queue with type 'log'; these messages must
have the logger module (module name), log level (from ```Levels```) and the message as a string:

    {"type": "log",
    "module": "blackjack",
    "level": "INFO",
    "message": "My message to the world"}

## Inter-Process Communication ##

For ease of use with our modules, we set up an IPC queue to allow message passing (in JSON)
without caring any more about how the modules function. Any IPC queue message that isn't flagged
as admin (or another recognized type) will simply be broadcast to all listeners. This allows modules
that care to follow up on those messages they care about and simply ignore any others. This format
is similar to our network messages.

* To register to listen to the IPC queue, simply include IPC and call ```getPipe```, which will give
a pipe. This pipe should be read, waiting for newlines to separate the JSON messages. To send a message
on this pipe, ensure that all complete JSON messages end in a newline, or messages may be discarded.
* Make sure you close this pipe when closing your module or on receiving the IPC 'shutdown' message
* When the IPC server is preparing for shutdown, a non-JSON 'shutdown' message will be sent (with newline)

## Server ##

The server listens for connections and passes off message duties to the Message Handler

## Message Handler ##

Allows an admin user to login (which allows access to the administration functions) and other users to
register their username. Only one user is allowed per username, the username will be deregistered when that
connection is ended. Any message types coming across the network that are valid JSON but unknown will be
forwarded to the IPC queue.

* ADMIN
* LOGIN
* ERROR - messages will be passed back across the network to allow users to see what went wrong
* APP - will be passed on
