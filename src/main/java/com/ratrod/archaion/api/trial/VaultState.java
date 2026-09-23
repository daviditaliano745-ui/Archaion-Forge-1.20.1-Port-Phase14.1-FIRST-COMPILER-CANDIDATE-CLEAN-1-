package com.ratrod.archaion.api.trial;

import net.minecraft.util.StringRepresentable;

/** 1.20.1 mirror of the 1.21 vault block states used by Archaion's existing models. */
public enum VaultState implements StringRepresentable {
    INACTIVE("inactive"), ACTIVE("active"), UNLOCKING("unlocking"), EJECTING("ejecting");
    private final String name;
    VaultState(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
