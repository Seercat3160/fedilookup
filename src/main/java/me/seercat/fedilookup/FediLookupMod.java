package me.seercat.fedilookup;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import draylar.omegaconfig.OmegaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FediLookupMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("fedilookup");

    public static FediLookupConfig CONFIG = OmegaConfig.register(FediLookupConfig.class);
    public static final FediLookupDataStorage DATA = OmegaConfig.register(FediLookupDataStorage.class);

    public static final SuggestionProvider<ServerCommandSource> KNOWN_PLAYER_SUGGESTION_PROVIDER = new KnownPlayerSuggestionProvider();
    public static final SuggestionProvider<ServerCommandSource> KNOWN_FEDI_ADDRESS_SUGGESTION_PROVIDER = new KnownFediAddressSuggestionProvider();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        CONFIG.save();
        DATA.save();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("fedi")
                        .then(
                                literal("set").then(argument("address", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            final String address = StringArgumentType.getString(context, "address");

                                            // get the name of the player
                                            final Text name = context.getSource().getPlayerOrThrow().getName();

                                            // validate the address, ensuring it is in the form `@user@domain`
                                            if (!address.matches("^@\\S+@\\S+$")) {
                                                context.getSource().sendError(Text.translatable("fedilookup.invalid_address_format").formatted(Formatting.RED));
                                                return 1;
                                            }

                                            // check whether the address is already used
                                            if (DATA.addresses.containsValue(address)) {
                                                context.getSource().sendError(Text.translatable("fedilookup.address_already_taken").formatted(Formatting.RED));
                                                return 1;
                                            }

                                            // set the address
                                            if (setAddress(context.getSource().getPlayerOrThrow().getUuid(), address)) {
                                                context.getSource().sendFeedback(() -> Text.translatable("fedilookup.set_address", name, formatAddress(address)).formatted(Formatting.GREEN), true);

                                            } else {
                                                context.getSource().sendError(Text.translatable("fedilookup.generic_failure").formatted(Formatting.RED));
                                            }
                                            return 1;
                                        }))
                        ).then(
                                literal("unset").executes(context -> {
                                    // get the UUID of the player
                                    final UUID uuid = context.getSource().getPlayerOrThrow().getUuid();

                                    // get the name of the player
                                    final Text name = context.getSource().getPlayerOrThrow().getName();

                                    // unset the address
                                    if (unsetAddress(uuid)) {
                                        context.getSource().sendFeedback(() -> Text.translatable("fedilookup.unset_address", name).formatted(Formatting.GREEN), true);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.translatable("fedilookup.no_address_set").formatted(Formatting.RED), false);
                                    }

                                    return 1;
                                })
                        ).then(literal("reload-config")
                                .requires(source -> source.hasPermissionLevel(4))
                                .executes(context -> {
                                    CONFIG = OmegaConfig.register(FediLookupConfig.class);
                                    context.getSource().sendFeedback(() -> Text.translatable("fedilookup.config_reload").formatted(Formatting.GREEN), true);
                                    return 1;
                                })
                        ).then(literal("who")
                                .then(argument("player", StringArgumentType.word())
                                        .suggests(KNOWN_PLAYER_SUGGESTION_PROVIDER)
                                        .executes(context -> {
                                            final String playerName = StringArgumentType.getString(context, "player");

                                            // get the UUID of the player in question
                                            Optional<GameProfile> gameProfileOptional = context.getSource().getServer().getUserCache().findByName(playerName);

                                            if (gameProfileOptional.isEmpty()) {
                                                context.getSource().sendError(Text.translatable("fedilookup.nonexistent_player").formatted(Formatting.RED));
                                                return 1;
                                            }

                                            UUID playerUUID = gameProfileOptional.get().getId();

                                            // get the address
                                            Optional<String> address = getAddress(playerUUID);
                                            if (address.isEmpty()) {
                                                context.getSource().sendFeedback(() -> Text.translatable("fedilookup.player_has_no_address"), false);
                                                return 1;
                                            }

                                            // send the address
                                            context.getSource().sendFeedback(() -> Text.translatable("fedilookup.forward_lookup_result", playerName, formatAddress(address.get())), false);

                                            return 1;
                                        })
                                )
                        ).then(literal("reverse")
                                .then(argument("address", StringArgumentType.greedyString())
                                        .suggests(KNOWN_FEDI_ADDRESS_SUGGESTION_PROVIDER)
                                        .executes(context -> {
                                            final String address = StringArgumentType.getString(context, "address");

                                            // get the player UUID
                                            Optional<UUID> uuid = getPlayerByAddress(address);
                                            if (uuid.isEmpty()) {
                                                context.getSource().sendFeedback(() -> Text.translatable("fedilookup.unknown_address"), false);
                                                return 1;
                                            }

                                            // get the player
                                            Optional<GameProfile> gameProfileOptional = context.getSource().getServer().getUserCache().getByUuid(uuid.get());
                                            if (gameProfileOptional.isEmpty()) {
                                                context.getSource().sendFeedback(() -> Text.translatable("fedilookup.user_cache_miss").formatted(Formatting.RED), true);
                                                return 1;
                                            }

                                            context.getSource().sendFeedback(() -> Text.translatable("fedilookup.reverse_lookup_result", formatAddress(address), gameProfileOptional.get().getName()), false);
                                            return 1;
                                        })
                                )
                        )
        ));

        LOGGER.info("FediLookup ready!");
    }

    /**
     * Set the associated fedi address for a player, overwriting any existing address or creating a new entry if needed.
     * If this returns `true`, the config has been saved to disk with the new data.
     * Doesn't currently perform any validation of the provided address, other than ensuring unique values.
     *
     * @param uuid    Player's UUID
     * @param address Fediverse address in the form `@user@domain.tld`
     * @return true on success, false otherwise.
     */
    boolean setAddress(@NotNull UUID uuid, @NotNull String address) {
        if (DATA.addresses.containsValue(address)) {
            return false;
        } else {
            DATA.addresses.put(uuid, address);
            DATA.save();
            return true;
        }
    }

    /**
     * Removes any associated fedi address of a player.
     * If this returns `true`, the config has been saved to disk with the new data.
     *
     * @param uuid Player's UUID
     * @return true if player had an address set, false otherwise.
     */
    boolean unsetAddress(@NotNull UUID uuid) {
        if (!DATA.addresses.containsKey(uuid)) {
            return false;
        } else {
            DATA.addresses.remove(uuid);
            DATA.save();
            return true;
        }
    }

    /**
     * Gets the associated address of a player.
     *
     * @param uuid Player's UUID
     */
    Optional<String> getAddress(@NotNull UUID uuid) {
        return Optional.ofNullable(DATA.addresses.get(uuid));
    }

    /**
     * Gets the associated Minecraft player of a fedi address.
     *
     * @param address Fedi address to check
     */
    Optional<UUID> getPlayerByAddress(@NotNull String address) {
        for (Map.Entry<UUID, String> entry : DATA.addresses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(address)) {
                return Optional.ofNullable(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Convenience method to format a fedi address for display in the chat.
     */
    Text formatAddress(@NotNull String address) {
        return Text.literal(address)
                .setStyle(
                        Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, address))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("fedilookup.copy_to_clipboard")))
                )
                .formatted(Formatting.UNDERLINE);
    }
}