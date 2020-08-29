# SE4AdminTool
A tool to help with administration of the Sniper Elite 4 Dedicated Server.
It is designed to be run on the command line and left to its own devices.
Written in java, so is portable across across a wide range of platforms: Windows, Mac, Linux and so on. 

## Basic Features
- Logs players joining and leaving the server by steam ID and IP address.
- Players can be automatically kicked from a server with the use of ban lists by steam ID and by IP address.
- Players can be **whitelisted** so that no ban check are performed. This is primarily there to allow
specific players access a server even if the have a steam VAC ban.
- A player greeting can be set up for whenever someone joins. See the sample configuration file for
an example.

## TODO
- Check steam account for VAC bans and auto-remove (unless whitelisted).
- Use IP location to find country, state and city information.

## Usage

No compiled version is provided as yet:
- Use the `mvn clean package` command to build
- Run with the provided batch file:   `seadmin.bat myserver.properties`
- if you want logging to a file, re-direct as usual: `seadmin.bat myserver.properties* > myserver.log`

## Configuration

An example properties file is included. At the very least you will need to change its contents to
match your server IP address, RCON port and RCON password.   The format of the sample player ban list,
ip address ban list and while list files is hopefully fairly self-explanatory.
 