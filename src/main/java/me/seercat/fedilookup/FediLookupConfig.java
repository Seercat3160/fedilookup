package me.seercat.fedilookup;

import draylar.omegaconfig.api.Config;

public class FediLookupConfig implements Config {
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
