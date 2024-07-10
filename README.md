
# FediLookup

A Fabric mod for Minecraft which allows players to set their fediverse address (e.g. `@seercat@example.com`) and query those of other players.

## Commands

- `/fedi who <player>`
    - Shows the fedi address of a player, if they have one set.
    - Autocomplete lists all players who have set an address, including offline players.
- `/fedi reverse <fedi address>`
    - Performs a reverse search to get the Minecraft user with a given fedi address.
    - Autocomplete lists all known fedi addresses.
- `/fedi set <fedi account>`
    - Associates your Minecraft account with the given fedi address.
- `/fedi set <player> <fedi address>` - Requires OP
    - Same as above but for another player.
    - Autocomplete for the player includes all players with addresses already, and all online players.
- `/fedi unset`
  - Removes any fedi account associations from yourself.
- `/fedi list` - Requires OP unless enabled for all in config
    - List all players with their associated fedi addresses.

## Configuration

Various things are configurable by the server admins using the config file:

- `allowSelfService` (boolean; default `true`)
    - Are users allowed to set their own fedi accounts or must an admin do it for them?
- `allowListCommand` (boolean; default `true`)
    - May ordinary players use the `/fedi list` command?

The config file also contains the stored data about accounts and players.
These parts should not be manually modified unless the server is shutdown, or ideally not at all.

## Future Plans

I plan to add WebFinger support, which will bring two things:
1. Any address given will be validated to ensure it's actually a fedi account which exists, and
2. The lookup will also include the URL of the account.