Uno
======

This is the server module for CS 3230

### Running the module

NodeJS must first be installed on the system. Navigate to the "server" directory (this
can be found on the `base_server` branch of this repository) and run `node server.js`.
Then, from an IDE or `.jar` file, start this server module listening on the same machine
(`Uno.jar`). Alternately, you can upload the `.jar` file to the server and run it
using _**TODO**_

### The following message types are handled by the server:

* _LOGIN_

```json
{
  "type": "login",
  "message": 
    {
      "username": "<user display name (must be unique)",
      "password": "<admin password>"  // this entry is optional
    }
}
```
* _CHAT_

```json
{
  "type": "chat",
  "message": "<chat text>"
}
```

* _WHOIS_

```json
{
  "type": "whois"
}
```

* _APP_

First message sent to the server must be a "start" message. When all desired players have joined, sending a "play"
message will cause the server to start the game. Drawing a card, performing a draw 2/4 action, or playing a card end
a turn. Currently, calling UNO is only supported for the player with UNO.

All messages will be wrapped in `{ "type": "application", "message": <message goes here> }`.

```json
{
  "module": "UNO",
  "action": "drawcard"
}
```

```json
{
  "module": "uno",
  "action": "send",
  "card":
    {
      "color": "red",
      "value": "1"
    }
}
```

```json
{
  "module": "uno",
  "action": "start"
}
```

```json
{
  "module": "uno",
  "action": "play"
}
```

```json
{
  "module": "uno",
  "action": "unocall"
}
```

```json
{
  "module": "uno",
  "action": "quit"
}
```

### The following message types may be sent from the server:

* _APP_ (The format of this message is dependant on the API defined by the group)

```json
{
  "playedCard":
    {
      "color": "color",
      "value": "1",
      "username": "username"
    }
}
```

```json
{
  "drawnCard":
    {
      "color": "color",
      "value": "1",
      "username": "username"
    }
}
```

```json
{
  "initialHand":
    [
      {"color": "color", "value": "value"}, 
      {"color": "color", "value": "value"},
      {"color": "color", "value": "value"},
      {"color": "color", "value": "value"}, 
      {"color": "color", "value": "value"}, 
      {"color": "color", "value": "value"}, 
      {"color": "color", "value": "value"}
    ]
}
```

```json
{
  "winnerMode": true
}
```

```json
{
  "turnMessage": "turn"
}
```

```json
{
  "quit": "quit"
}
```

```json
{
  "win":
    {
      "winMessage": "win",
      "username": "username"
    }
}
```

* _WHOIS_

```json
{
  "type": "whois",
  "message":
    {
      "users":
        [
          {
            "username": "<user name>",
            "modules":
              [
                "<module name>"
              ]
          }
        ],
      "modules": 
        [
          {
            "moduleName": "<module name>",
            "started": <boolean>
          }
        ]
    }
}
```

* _ACK_

```json
{
  "type": "acknowledge",
  "message": "<message from server>"
}
```

* _ERROR_

```json
{
  "type": "error",
  "message": "<error message from server>"
}
```
