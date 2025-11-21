package com.myaddon.iuaddon.items.modules;

import com.myaddon.iuaddon.IUAddon;

public enum EnumAddonUpgradeModules {
    WATER_GENERATOR(0, "water_generator", "Генерирует воду в машинах с жидкостными резервуарами");
    
    public final int id;
    public final String name;
    public final String description;
    
    EnumAddonUpgradeModules(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public static EnumAddonUpgradeModules getFromID(final int ID) {
        return values()[ID % values().length];
    }
}