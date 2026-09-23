package com.ratrod.archaion.block;

import net.minecraft.util.StringRepresentable;

public enum TeleporterColor implements StringRepresentable {
    WHITE("white", -1),
    RED("red", -2076592),
    ORANGE("orange", -875716),
    YELLOW("yellow", -862916),
    GREEN("green", -10758309),
    CYAN("cyan", -11544360),
    BLUE("blue", -11895312),
    PURPLE("purple", -4887824);

    private final String name;
    private final int color;
    TeleporterColor(String name, int color) { this.name = name; this.color = color; }
    @Override public String getSerializedName() { return name; }
    public int argb() { return color; }
    public TeleporterColor next() { TeleporterColor[] values = values(); return values[(ordinal() + 1) % values.length]; }
}
