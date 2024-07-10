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

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KnownFediAddressSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        // get the list of fedi addresses
        List<String> addresses = FediLookupMod.DATA.addresses.values().stream().toList();

        for (String address : addresses) {
            if (CommandSource.shouldSuggest(builder.getRemaining(), address)) {
                builder.suggest(address);
            }
        }

        return builder.buildFuture();
    }
}
