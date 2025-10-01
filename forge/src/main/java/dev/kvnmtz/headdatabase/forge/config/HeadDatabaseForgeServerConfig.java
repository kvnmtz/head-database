package dev.kvnmtz.headdatabase.forge.config;

import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

public class HeadDatabaseForgeServerConfig implements HeadDatabaseServerConfig {

    private static HeadDatabaseForgeServerConfig INSTANCE;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<PermissionMode> PERMISSION_MODE;
    public static final ForgeConfigSpec.IntValue REQUIRED_OP_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELIST;

    static {
        BUILDER.push("permissions");

        PERMISSION_MODE = BUILDER
                .comment("OP_ONLY: Operators only")
                .comment("WHITELIST: Operators and additional whitelisted players")
                .comment("EVERYONE: No permission check")
                .defineEnum("mode", PermissionMode.OP_ONLY);

        REQUIRED_OP_LEVEL = BUILDER
                .comment("Required operator level (0-4) when using OP_ONLY / WHITELIST mode")
                .defineInRange("required_op_level", 2, 0, 4);

        WHITELIST = BUILDER
                .comment("List of non-operator player names or UUIDs allowed to use the command when using WHITELIST " +
                        "mode")
                .defineList("whitelist", List.of(), obj -> obj instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private HeadDatabaseForgeServerConfig() {}

    public static HeadDatabaseForgeServerConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HeadDatabaseForgeServerConfig();
        }
        return INSTANCE;
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    @Override
    public PermissionMode getPermissionMode() {
        return PERMISSION_MODE.get();
    }

    @Override
    public int getRequiredOpLevel() {
        return REQUIRED_OP_LEVEL.get();
    }

    @Override
    public List<String> getWhitelist() {
        //noinspection unchecked
        return (List<String>) WHITELIST.get();
    }

    @Override
    public void reload() {
        SPEC.afterReload();
    }
}
