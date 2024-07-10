package me.seercat.fedilookup;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;

public class FediLookupConfig implements Config {
    @Comment(value = " Are users allowed to set and unset their own fedi accounts or must an admin do it for them?")
    boolean allowSelfService = true;

    @Comment(value = " May ordinary players use the \"/fedi list\" command?")
    boolean allowListCommand = true;

    @Override
    public String getName() {
        return "fedilookup-config";
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
