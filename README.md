# SE4AdminTool
A tool to help with administration of the Sniper Elite 4 Dedicated Server. There are two versions:
a command line tool intended to be run and left to its own devices (typically on a 24x7
online or home server) and an interactive GUI version.

The tool is written in java to be portable across a wide range of platforms: Windows, Mac, Linux etc. 

## Basic Features - Command Line Version
- Logs players joining and leaving the server by steam ID and IP address.
- Players can be automatically kicked from a server with the use of ban lists by steam ID and by IP address.
- Players can be **whitelisted** so that ban checks are skipped. This is primarily there to allow
specific players access a server even if they have a steam VAC ban.
- A friendly player greeting can be set up for whenever someone joins. See the sample configuration file for
an example.
- Players can be automatically kicked if they have a steam VAC ban and/or a Game ban.  Note that this feature requires
you to provide your own Steam API Key. Obtain one from <https://steamcommunity.com/dev/apikey> and
add it to your configuration file.  Note that Game bans could for any game, not just Sniper Elite.
For a guide to steam policies refer to <https://steamcommunity.com/sharedfiles/filedetails/?id=961168214>,
particularly section 5.  

## Basic Features - GUI Version
- All features of command line version
- Player information display during a game including steam ID, score, kills, longest shot.
- Kick or Ban a player.
- See players' steam profile

## TODO
- Use IP location to find country, state and city information.
- Players banned by GUI tool should be added to tools' ban list automatically.
- Add facility to send server commands via GUI tool.
- Create configuration properties files via GUI.
 

## Pre-Release Version
A windows pre release version of the GUI version can be downloaded from <https://github.com/peterwalkley/SE4AdminTool/releases/download/0.1-beta/TFA.SE4.Administrator_0.0.1-SNAPSHOT.exe>. 
This will install the GUI and a java runtime environment for the application. Before using the GUI for the first time
you will need to set up properties files for your own servers.  Use the supplied `config.properties` file as a template
and create a file for each server you wish to administer.  You can open connections to multiple servers at the
same time. Each will appear in its own application tab.

## Developer Usage

- Use the `mvn clean install` command to build
- Run with the provided batch file:   `seadmin.bat myserver.properties`
- if you want logging to a file, re-direct as usual: `seadmin.bat myserver.properties* > myserver.log`

## Configuration

An example properties file is included. At the very least you will need to change its contents to
match your server IP address, RCON port and RCON password. Include your steam API key if you
want to be able to check for VAC bans. The format of the sample player ban list,
IP address ban list and white list files is hopefully fairly self-explanatory.
 