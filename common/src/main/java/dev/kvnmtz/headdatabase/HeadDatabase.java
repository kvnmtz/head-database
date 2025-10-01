package dev.kvnmtz.headdatabase;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.kvnmtz.headdatabase.commands.HdbCommand;
import dev.kvnmtz.headdatabase.data.HeadDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HeadDatabase {

    public static final String MOD_ID = "head_database";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        CommandRegistrationEvent.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> HdbCommand.register(commandDispatcher));

        LifecycleEvent.SERVER_STARTED.register(minecraftServer -> {
            LOGGER.info("Initializing Head Database...");
            HeadDataManager.getInstance().initialize().whenComplete((result, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Failed to initialize Head Database", throwable);
                }
            });
        });
    }
}
