package mrnavastar.quantum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mrnavastar.quantum.util.Settings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class ConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reloadconfig")
                .executes(ConfigCommand::reloadConfig)
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Settings.init();
        source.sendFeedback(new LiteralText("Reloaded Config!"), false);
        return 1;
    }
}