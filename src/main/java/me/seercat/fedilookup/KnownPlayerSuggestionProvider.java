package me.seercat.fedilookup;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.UserCache;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class KnownPlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        // get the list of player UUIDs which have associated fedi addresses
        Set<UUID> uuids = FediLookupMod.DATA.addresses.keySet();

        // transform the list of UUIDs into a list of player names
        UserCache userCache = context.getSource().getServer().getUserCache();

        for (UUID uuid : uuids) {
            Optional<GameProfile> optionalProfile = userCache.getByUuid(uuid);

            optionalProfile.ifPresent(gameProfile -> {
                if (CommandSource.shouldSuggest(builder.getRemaining(), gameProfile.getName())) {
                    builder.suggest(gameProfile.getName());
                }
            });
        }

        return builder.buildFuture();
    }
}
