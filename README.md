# XBF Core
Xervin Bot Framework

## Build
![Development Build](https://github.com/XervinBF/Xervin5/workflows/Development%20Build/badge.svg)
![Release Build](https://github.com/XervinBF/Xervin5/workflows/Release%20Build/badge.svg)

## What is this?
Xerbin Bot Framework / XervinBF / **XBF** or whatever you would like to call it. Is one of the most powerful bot frameworks with native support for databases and multiple ways to host your bot. Want to host a discord bot, use the discord bot plugin, maybe a slack bot, use the slack bot plugin. Or maybe even both bots in the same application. That is no problem. 

## But how?
**XBF** uses plugins built by the community. Some plugins are provided by the authors.

## What plugins can i make?
You can make your own:
- Commands
- Backend services
- Database Providers
- Bot Handlers
- ChatHandlers

## But what is all of this?

### Commands
The commands a divided into modules. A plugin can register multiple modules or even use another plugins modules.

### Backend Services
Backend Services are threads that the developers can make to run jobs in the background. The Administrator can start and stop services with commands too.

### Database Providers
A Database provider is a class that implements some methods from `IDBProvider` and must have a `DbType` annotation with the Database type being provided. The IDBProvider is then registered upon the call of the register method in the plugin class.

### Bot Handlers
A bot handler is a class that the developer can register their bot integration in. The bot handler is supposed to build a `Request` for the `CommandProcessor` to handle. Then the `CommandProcessor` responds with a `Response` wich is the response the bot is supposed to respond with.

### ChatHandlers
A ChatHandler is for NON-COMMANDS (text that does not start with the specified prefix), the chat handler can respond with a `Response` or maybe just a message removal of the message currently being processed.

## Ok how do i start?
To get started refer to the [Example Plugin](https://github.com/XervinBF/ExamplePlugin)

## But i want to host
- [ ] To host the bot, simply start the xbf-core-\*.\*.\*-jar-with-dependencies.jar file. 
- [ ] Then place your plugins into the plugin folder (or whatever folder specified in config.yml). 
- [ ] Optionally specify a custom name for the bot to use in commands in the config.yml too. 
- [ ] A database provider plugin is required for the bot to work because the configuration values for the bot is stored in the database along with all user data. 
- [ ] The database must be created before use (otherwise errors will occur). Then the database tables will set themself up as needed.
