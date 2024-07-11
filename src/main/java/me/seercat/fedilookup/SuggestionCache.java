package me.seercat.fedilookup;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.UserCache;

import java.util.*;

/**
 * Whereas the data store on disk maps player UUIDs to fedi addresses, this class just keeps a list of player names and a list of fedi addresses.
 * It's main purpose is to optimise the generation of command completions by removing the need to perform lookups of UUIDs to names all the time.
 * If we want to get a UUID from a name or vice versa, we use the Minecraft UserCache instead. This is just a list of names we know.
 */
public class SuggestionCache {
    private final Set<String> addresses = new HashSet<>();
    private final Set<String> names = new HashSet<>();

    // Is this cache ready to serve suggestions?
    private boolean ready = false;

    // Have we ever built this cache?
    private boolean hasPerformedInitialBuild = false;

    public boolean hasNotPerformedInitialBuild() {
        return !hasPerformedInitialBuild;
    }

    public Set<String> getAddresses() {
        if (!ready) {
            return Set.of();
        }

        return addresses;
    }

    public Set<String> getNames() {
        if (!ready) {
            return Set.of();
        }

        return names;
    }

    public void rebuild(UserCache userCache, Collection<UUID> newUuids, Collection<String> newAddresses) {
        // no longer ready
        this.ready = false;

        // we've performed a cache build now
        this.hasPerformedInitialBuild = true;

        // clear the old data
        this.addresses.clear();
        this.names.clear();

        // get a name for each player UUID
        for (UUID uuid : newUuids) {
            Optional<GameProfile> optionalProfile = userCache.getByUuid(uuid);

            optionalProfile.ifPresent(gameProfile -> this.names.add(gameProfile.getName()));
        }

        // add the addresses we've been provided
        this.addresses.addAll(newAddresses);

        // we're now ready
        this.ready = true;
    }
}
