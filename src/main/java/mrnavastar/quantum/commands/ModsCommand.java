package mrnavastar.quantum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mrnavastar.quantum.api.ModrinthAPI;
import mrnavastar.quantum.util.ModManager;
import mrnavastar.quantum.util.Settings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;

import java.util.HashMap;

public class ModsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("listmods").requires(source -> source.hasPermissionLevel(4))
                .executes(ModsCommand::listMods)
        );

        dispatcher.register(CommandManager.literal("updatemods").requires(source -> source.hasPermissionLevel(4))
                .executes(ModsCommand::updateMods)
        );

        dispatcher.register(CommandManager.literal("downloadmods").requires(source -> source.hasPermissionLevel(4))
                .executes(ModsCommand::downloadMods)
        );
    }

    private static int listMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (Settings.getServerSideMods().size() > 0) source.sendFeedback(new LiteralText("--------Server-Side-Mods--------"), false);
        Settings.getServerSideMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version), false));
        if (Settings.getRequiredMods().size() > 0) source.sendFeedback(new LiteralText("--------Required-Mods-----------"), false);
        Settings.getRequiredMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version), false));
        if (Settings.getRecommendedMods().size() > 0) source.sendFeedback(new LiteralText("--------Recommended-Mods--------"), false);
        Settings.getRecommendedMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version), false));
        return 1;
    }

    private static int downloadMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Downloading..."), false);
        Settings.getServerSideMods().forEach((name, version) -> {
            ModrinthAPI.downloadMod(version.toString());
            source.sendFeedback(new LiteralText("Downloaded: " + name + " | " + version), false);
        });
        Settings.getRequiredMods().forEach((name, version) -> {
            ModrinthAPI.downloadMod(version.toString());
            source.sendFeedback(new LiteralText("Downloaded: " + name + " | " + version), false);
        });
        Settings.getRecommendedMods().forEach((name, version) -> {
            ModrinthAPI.downloadMod(version.toString());
            source.sendFeedback(new LiteralText("Downloaded: " + name + " | " + version), false);
        });
        source.sendFeedback(new LiteralText("Done. Changes will take effect on the next reboot"), false);
        return 1;
    }

    private static int updateMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Fetching..."), false);

        HashMap<String, Pair<String, String>> updates = ModrinthAPI.getUpdates();
        if (updates.size() > 0) {
            source.sendFeedback(new LiteralText("Found " + updates.size() + " Updates!"), false);
            updates.forEach((name, versions) -> {
                ModManager.updateMod(versions);
                source.sendFeedback(new LiteralText("Updated: " + name + " | " + versions.getLeft() + " -> " + versions.getRight()), false);
            });
            source.sendFeedback(new LiteralText("Done. Changes will take effect on the next reboot"), false);
            return 1;
        }

        source.sendFeedback(new LiteralText("No updates found"), false);
        return 1;
    }
}