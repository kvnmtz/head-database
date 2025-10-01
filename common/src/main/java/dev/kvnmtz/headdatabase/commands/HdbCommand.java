package dev.kvnmtz.headdatabase.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import dev.kvnmtz.headdatabase.data.HeadDataManager;
import dev.kvnmtz.headdatabase.permissions.PermissionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import dev.kvnmtz.headdatabase.gui.HeadDatabaseGui;

public class HdbCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hdb")
                .requires(PermissionManager::hasPermission)
                .executes(HdbCommand::execute)
                .then(Commands.literal("search")
                        .then(Commands.argument("query", StringArgumentType.greedyString())
                                .executes(HdbCommand::executeSearch)))
                .then(Commands.literal("reload")
                        // console or op level 2+
                        .requires(source -> source.getEntity() == null || source.hasPermission(2))
                        .executes(HdbCommand::executeReload)));
    }

    private static boolean setup(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return false;
        }

        var headManager = HeadDataManager.getInstance();

        if (!headManager.isLoaded()) {
            if (headManager.isLoading()) {
                source.sendFailure(
                        Component.literal("Head database is still loading, please try again in a moment..."));
            } else {
                source.sendFailure(Component.literal("Head database is not available. Check server logs for errors."));
            }
            return false;
        }

        return true;
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();

        if (!setup(source)) {
            return 0;
        }

        //noinspection DataFlowIssue
        HeadDatabaseGui.openCategoryGui((ServerPlayer) source.getEntity());
        return 1;
    }

    private static int executeSearch(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();

        if (!setup(source)) {
            return 0;
        }

        var headManager = HeadDataManager.getInstance();

        var query = StringArgumentType.getString(context, "query");
        var searchResults = headManager.searchHeads(query);

        if (searchResults.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§cNo heads found matching: " + query), false);
            return 1;
        }

        //noinspection DataFlowIssue
        HeadDatabaseGui.openSearchResultsGui((ServerPlayer) source.getEntity(), searchResults, query);
        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();

        try {
            HeadDatabaseServerConfig.get().reload();

            var server = source.getServer();
            var onlinePlayers = server.getPlayerList().getPlayers();

            // send updated commands to all online players
            for (var player : onlinePlayers) {
                server.getCommands().sendCommands(player);
            }

            var isConsole = source.getEntity() == null;
            source.sendSuccess(() -> Component.literal("§aHead Database configuration reloaded successfully!"),
                    !isConsole);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cFailed to reload configuration: " + e.getMessage()));
            return 0;
        }
    }
}