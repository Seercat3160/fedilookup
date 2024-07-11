package me.seercat.fedilookup;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class KnownPlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        // perform a build of the suggestion cache if it hasn't been done yet (needed because we don't have access to the server's UserCache when we initialise the cache)
        if (FediLookupMod.SUGGESTION_CACHE.hasNotPerformedInitialBuild()) {
            FediLookupMod.SUGGESTION_CACHE.rebuild(context.getSource().getServer().getUserCache(), FediLookupMod.DATA.addresses.keySet(), FediLookupMod.DATA.addresses.values().stream().toList());
        }

        return CommandSource.suggestMatching(FediLookupMod.SUGGESTION_CACHE.getNames(), builder);
    }
}
