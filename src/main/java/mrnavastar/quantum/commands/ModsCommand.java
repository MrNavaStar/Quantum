package mrnavastar.quantum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mrnavastar.quantum.api.ModrinthAPI;
import mrnavastar.quantum.services.ModManager;
import mrnavastar.quantum.util.Settings;
import mrnavastar.quantum.util.datatypes.Mod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ModsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mods").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("list").executes(ModsCommand::listMods))
                .then(CommandManager.literal("sync").executes(ModsCommand::syncMods))
                .then(CommandManager.literal("update").executes(ModsCommand::updateMods))

                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("type", StringArgumentType.string()).suggests((context, builder) -> builder.suggest("server").suggest("required").suggest("recommended").buildFuture())
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .then(CommandManager.argument("version_id", StringArgumentType.string())
                                                .executes(context -> addMod(context, StringArgumentType.getString(context, "type"), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "version_id")))
                                        )
                                )
                        )
                )

                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string()).suggests((context, builder) -> {
                                    for (Mod mod : ModManager.downloadedMods.values()) builder.suggest(mod.getName());
                                    return builder.buildFuture();
                                })
                                .executes(context -> removeMod(context, StringArgumentType.getString(context, "name")))
                        )
                )
        );
    }

    private static int listMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        Settings.init();
        if (Settings.getServerSideMods().size() > 0) source.sendFeedback(new LiteralText("--------Server-Side-Mods--------"), false);
        Settings.getServerSideMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version + " | Downloaded: " + ModManager.isDownloaded(version.toString())), false));
        if (Settings.getRequiredMods().size() > 0) source.sendFeedback(new LiteralText("--------Required-Mods-----------"), false);
        Settings.getRequiredMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version + " | Downloaded: " + ModManager.isDownloaded(version.toString())), false));
        if (Settings.getRecommendedMods().size() > 0) source.sendFeedback(new LiteralText("--------Recommended-Mods--------"), false);
        Settings.getRecommendedMods().forEach((name, version) -> source.sendFeedback(new LiteralText(name + " | " + version + " | Downloaded: " + ModManager.isDownloaded(version.toString())), false));
        return 1;
    }

    private static int syncMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ArrayList<Mod> keepingMods = new ArrayList<>();
        source.sendFeedback(new LiteralText("Syncing with config..."), false);

        Runnable runnable = () -> {
            Settings.init();
            Settings.getServerSideMods().forEach((name, version) -> {
                Mod mod = new Mod(name, version.toString(), "server");
                keepingMods.add(mod);
            });
            Settings.getRequiredMods().forEach((name, version) -> {
                Mod mod = new Mod(name, version.toString(), "required");
                keepingMods.add(mod);
            });
            Settings.getRecommendedMods().forEach((name, version) -> {
                Mod mod = new Mod(name, version.toString(), "recommended");
                keepingMods.add(mod);
            });

            keepingMods.forEach(mod -> {
                if (!ModManager.isDownloaded(mod.getVersionId())) {
                    ModManager.downloadMod(mod);
                    source.sendFeedback(new LiteralText("Downloaded: " + mod.getName() + " | " + mod.getVersionId()), false);
                } else source.sendFeedback(new LiteralText("Skipping: " + mod.getName()), false);
            });

            ModManager.pruneMods(keepingMods).forEach(mod -> source.sendFeedback(new LiteralText("Removed: " + mod.getName() + " | " + mod.getVersionId()), false));
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return 1;
    }

    private static int updateMods(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Fetching..."), false);

        Runnable runnable = () -> {
            Settings.init();
            HashMap<String, Pair<String, String>> updates = ModrinthAPI.getUpdates();
            if (updates.size() > 0) {
                source.sendFeedback(new LiteralText("Found " + updates.size() + " Updates!"), false);
                updates.forEach((name, versions) -> {
                    ModManager.updateMod(name, versions);
                    source.sendFeedback(new LiteralText("Updated: " + name + " | " + versions.getLeft() + " -> " + versions.getRight()), false);
                });
            } else source.sendFeedback(new LiteralText("No updates found"), false);
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return 1;
    }

    private static int addMod(CommandContext<ServerCommandSource> context, String type, String name, String versionId) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Adding mod..."), false);

        Runnable runnable = () -> {
            Settings.init();
            if (type.equals("server")) Settings.getServerSideMods().put(name, versionId);
            if (type.equals("required")) Settings.getRequiredMods().put(name, versionId);
            if (type.equals("recommended")) Settings.getRecommendedMods().put(name, versionId);
            Settings.saveConfigState();

            ModManager.downloadMod(new Mod(name, versionId, type));
            source.sendFeedback(new LiteralText("Downloaded: " + name + " | " + versionId), false);
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return 1;
    }

    private static int removeMod(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();

        String versionId = (String) Settings.getServerSideMods().get(name);
        if (versionId == null) versionId = (String) Settings.getRequiredMods().get(name);
        if (versionId == null) versionId = (String) Settings.getRecommendedMods().get(name);

        if (versionId != null) {
            ModManager.removeMod(ModManager.downloadedMods.get(versionId));
            source.sendFeedback(new LiteralText("Removed: " + name + " | " + versionId), false);
        } else source.sendFeedback(new LiteralText("There is no mod with that name"), false);
        return 1;
    }
}