package me.seercat.fedilookup;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import draylar.omegaconfig.OmegaConfig;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.*;

public class FediLookupMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("fedilookup");

	public static final FediLookupConfig CONFIG = OmegaConfig.register(FediLookupConfig.class);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CONFIG.save();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("fedi")
				.then(
						literal("set").then(argument("address", StringArgumentType.greedyString())
								.executes(context -> {
									final String address = StringArgumentType.getString(context, "address");

									// validate the address, ensuring it is in the form `@user@domain`
									if (!address.matches("^@\\S+@\\S+$")) {
										context.getSource().sendError(Text.literal("Invalid address! Please use the form @user@domain").withColor(0xFF0000)
										);
										return 1;
									}

									// check whether the address is already used
									if (CONFIG.addresses.containsValue(address)) {
										context.getSource().sendError(Text.literal("That address is already taken!").withColor(0xFF0000));
										return 1;
									}

									// set the address
									if (setAddress(context.getSource().getPlayerOrThrow().getUuid(), address)) {
										context.getSource().sendFeedback(() -> Placeholders.parseText(
												Text.literal("Set the fedi address of %player:name% to " + address + ".").withColor(0x00FF00),
												PlaceholderContext.of(context.getSource())), true);

                                    } else {
										context.getSource().sendError(Text.literal("Something went wrong! Talk to your admin for help.").withColor(0xFF0000));
                                    }
                                    return 1;
                                }))
				).then(literal("unset").executes(context -> {
					// get the UUID of the player
					UUID uuid = context.getSource().getPlayerOrThrow().getUuid();

					// unset the address
					if (unsetAddress(uuid)) {
						context.getSource().sendFeedback(() -> Placeholders.parseText(
								Text.literal("Unset the fedi address of %player:name%.").withColor(0x00FF00),
								PlaceholderContext.of(context.getSource())), true);
					} else {
						context.getSource().sendFeedback(() -> Text.literal("You don't have a fedi address set.").withColor(0x00FF00), false);
					}

					return 1;
				}))
		));

		LOGGER.info("FediLookup ready!");
	}

	/**
	 * Set the associated fedi address for a player, overwriting any existing address or creating a new entry if needed.
	 * If this returns `true`, the config has been saved to disk with the new data.
	 * Doesn't currently perform any validation of the provided address, other than ensuring unique values.
	 * @param uuid Player's UUID
	 * @param address Fediverse address in the form `@user@domain.tld`
	 * @return true on success, false otherwise.
	 */
	boolean setAddress(@NotNull UUID uuid, @NotNull String address) {
		if (CONFIG.addresses.containsValue(address)) {
			return false;
		} else {
			CONFIG.addresses.put(uuid, address);
			CONFIG.save();
			return true;
		}
	}

	/**
	 * Removes any associated fedi address of a player.
	 * If this returns `true`, the config has been saved to disk with the new data.
	 * @param uuid Player's UUID
	 * @return true if player had an address set, false otherwise.
	 */
	boolean unsetAddress(@NotNull UUID uuid) {
		if (!CONFIG.addresses.containsKey(uuid)) {
			return false;
		} else {
			CONFIG.addresses.remove(uuid);
			CONFIG.save();
			return true;
		}
	}

	/**
	 * Gets the associated address of a player.
	 * @param uuid Player's UUID
	 */
	Optional<String> getAddressOfPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(CONFIG.addresses.get(uuid));
	}

	/**
	 * Gets the associated Minecraft player of a fedi address.
	 * @param address Fedi address to check
	 */
	Optional<UUID> getPlayerByAddress(@NotNull String address) {
		for (Map.Entry<UUID, String> entry : CONFIG.addresses.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(address)) {
				return Optional.ofNullable(entry.getKey());
			}
		}
		return Optional.empty();
	}
}