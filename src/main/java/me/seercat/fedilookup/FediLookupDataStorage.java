package me.seercat.fedilookup;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FediLookupDataStorage implements Config {

    @Comment(value = " Mapping of player UUID to fedi address - Don't touch!")
    Map<UUID, String> addresses = new HashMap<>();

    @Override
    public String getName() {
        return "fedilookup-data";
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
