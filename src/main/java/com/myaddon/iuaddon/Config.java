package com.myaddon.iuaddon;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class Config {
    
    public static Configuration config;
    
    // Настройки модуля генерации воды
    public static int waterGeneratorRate = 20000; // мБ/тик (20 ведер)
    public static int waterGeneratorEnergyConsumption = 100; // EU/тик дополнительного потребления
    
    public static void init(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "iu_addon.cfg");
        config = new Configuration(configFile);
        
        try {
            config.load();
            
            // Категория для модулей
            String categoryModules = "modules";
            config.addCustomCategoryComment(categoryModules, "Настройки модулей IU Addon");
            
            waterGeneratorRate = config.getInt(
                "waterGeneratorRate", 
                categoryModules, 
                20000, 
                1, 
                100000, 
                "Скорость генерации воды модулем в мБ/тик (1000 мБ = 1 ведро)"
            );
            
            waterGeneratorEnergyConsumption = config.getInt(
                "waterGeneratorEnergyConsumption", 
                categoryModules, 
                100, 
                0, 
                10000, 
                "Дополнительное потребление энергии модулем генерации воды в EU/тик"
            );
            
        } catch (Exception e) {
            System.err.println("IU Addon: Ошибка загрузки конфигурации!");
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}