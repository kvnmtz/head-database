package dev.kvnmtz.headdatabase.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import dev.kvnmtz.headdatabase.forge.config.HeadDatabaseForgeServerConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import dev.kvnmtz.headdatabase.HeadDatabase;

@Mod(HeadDatabase.MOD_ID)
public final class HeadDatabaseForge {

    public HeadDatabaseForge(FMLJavaModLoadingContext context) {
        HeadDatabaseForgeServerConfig.register(context);
        HeadDatabaseServerConfig.setInstance(HeadDatabaseForgeServerConfig.getInstance());

        EventBuses.registerModEventBus(HeadDatabase.MOD_ID, context.getModEventBus());
        HeadDatabase.init();
    }
}
