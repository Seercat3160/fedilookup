package me.seercat.fedilookup;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FediLookupConfig implements Config {
    @Comment(value = " Are users allowed to set their own fedi accounts or must an admin do it for them?")
    boolean allowSelfService = true;

    @Comment(value = " May ordinary players use the \"/fedi list\" command?")
    boolean allowListCommand = true;

    @Comment(value = " Mapping of player UUID to fedi address - Don't touch!")
    Map<UUID, String> addresses = new HashMap<>();

    @Override
    public String getName() {
        return "fedilookup";
    }

    @Override
    public String getExtension() {
        return "json5";
    }

    @Override
    public String getDirectory() {
        return "fedilookup";
    }
}
