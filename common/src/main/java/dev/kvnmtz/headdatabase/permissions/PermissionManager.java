package dev.kvnmtz.headdatabase.permissions;

import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PermissionManager {

    public static boolean hasPermission(CommandSourceStack source) {
        var isConsole = source.getEntity() == null;
        if (isConsole) {
            return true;
        }

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return false;
        }
        
        var mode = HeadDatabaseServerConfig.get().getPermissionMode();

        return switch (mode) {
            case OP_ONLY -> hasOpPermission(source);
            case WHITELIST -> hasOpPermission(source) || isWhitelisted(player);
            case EVERYONE -> true;
        };
    }
    
    private static boolean hasOpPermission(CommandSourceStack source) {
        var requiredLevel = HeadDatabaseServerConfig.get().getRequiredOpLevel();
        return source.hasPermission(requiredLevel);
    }
    
    private static boolean isWhitelisted(ServerPlayer player) {
        var whitelist = HeadDatabaseServerConfig.get().getWhitelist();
        var playerName = player.getGameProfile().getName();
        var playerUuid = player.getStringUUID();
        
        // check for both username and UUID
        return whitelist.contains(playerName) || whitelist.contains(playerUuid);
    }
}