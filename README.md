
# FediLookup

<a href="https://modrinth.com/mod/fedilookup"><img alt="modrinth" height="28" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2/assets/compact/available/modrinth_vector.svg"></a> <a href="https://github.com/Seercat3160/fedilookup"><img alt="github" height="28" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2/assets/compact/available/github_vector.svg"></a>

A Fabric mod for Minecraft which allows players to set their fediverse address (e.g. `@seercat@example.com`) and query those of other players.

## Commands

- `/fedi who <player>`
  - Shows the fedi address of a player, if they have one set.
  - Autocomplete lists all players who have set an address, including offline players.
- `/fedi reverse <address>`
  - Performs a reverse search to get the Minecraft user with a given fedi address.
  - Autocomplete lists all known fedi addresses.
- `/fedi set <address>`
  - Associates your Minecraft account with the given fedi address.
- `/fedi set-other <player> <address>` - Requires OP
  - Associates a specified Minecraft account with the given fedi address.
- `/fedi unset`
  - Removes any fedi account associations from yourself.
- `/fedi unset [<player>]` - Requires OP
  - Removes any fedi account associations from a specified player.
  - Autocomplete lists all players who have set an address, including offline players.
- `/fedi reload-config` - Requires OP
  - Reloads the configuration file from disk.

## Configuration

There is nothing configurable at the moment.
The main config file can be reloaded at runtime by an operator using `/fedi reload-config`.

An additional file contains the stored data about accounts and players.
This file should not be manually modified unless the server is shutdown, or ideally not at all.

## Future Plans

I plan to add WebFinger support, which will bring two things:
1. Any address given will be validated to ensure it's actually a fedi account which exists, and
2. The lookup will also include the web URL of the account.