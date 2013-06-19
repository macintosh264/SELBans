=== What is it? ===

SELBans is an advanced banning, kicking, warning and muting system developed to store all records in a database for easy retrieving of a players stats. Since it's SQL it is very fast and efficient, and can be accessed from multiple locations and languages such as PHP (to make a web interface).

---

=== How it works ===

All Bans, kicks warnings and mutes are stored into a database, they act the same as any other plugin, but allow for lookups, even if a player has been pardoned or unmuted. Acting as a permanent record on a player, the plugin can show a previous players banning, kicking, warning or muting history and lets the admins decide how to deal with the player.

Main features of the plugin:
* Store Bannings, kickings, mutes and Warnings into a database.
* Lookup a specifc players history for bans, kicks, mutes and warnings
* Lookup where a player was banned from console
* Do timed based banning and mutes.
* Centralize bannings for multiple servers.
* Block specific commands for muted players
* Block player death messages for muted players.
* Custom kick, warning, muting, banning and more messages.

---

=== Commands ===

**ban**: Bans a player with a specified reason.<br />
**kick**: Kicks a player with a specified reason.<br />
**warn**: Warns a player with a message.<br />
**pardon**: Unbans a player.<br />
**mute**: Warns a player with a message.<br />
**playerinfo**: Displays Player Info.<br />
**SELBans**: Reloads the SELBans Configuration.<br />

---

=== Permissions ===

**SELBans.***: Gives access to all SELBans Permissions.<br />
**SELBans.ban**: Allows access to the /ban command.<br />
**SELBans.ban.notify**: Shows notification when a player is banned.<br />
**SELBans.warn**: Allows access to the /warn command.<br />
**SELBans.warn.notify**: Shows notification when a player is warned.<br />
**SELBans.kick**: Allows access to the /kick command.<br />
**SELBans.kick.notify**: Shows a notification when a player is kicked.<br />
**SELBans.mute**: Allows access to the /mute command.<br />
**SELBans.mute.notify**: Shows a notification when a player is muted.<br />
**SELBans.playerinfo**: Allows access to the /warns, /kicks, /bans and /warns lookup commands.<br />
**SELBans.reload**: Allows access to the /SELBans command, and reload the SELBans configuration.<br />
