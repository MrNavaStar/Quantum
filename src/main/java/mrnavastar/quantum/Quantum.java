package mrnavastar.quantum;

import com.mojang.blaze3d.systems.RenderSystem;
import mrnavastar.quantum.api.ServerSyncAPI;
import mrnavastar.quantum.commands.ModsCommand;
import mrnavastar.quantum.services.Launcher;
import mrnavastar.quantum.services.ModManager;
import mrnavastar.quantum.util.FileHelpers;
import mrnavastar.quantum.util.Settings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;

public class Quantum implements ModInitializer {

    public static final String MODID = "Quantum";
    public static String gameVersion;
    public static Path dir;

    @Override
    public void onInitialize() {
        if (!FileHelpers.supportedFileSystem()) {
            log(Level.ERROR, "Quantum failed to start! Unsupported FileSystem!");
            return;
        }

        Settings.init();
        ModManager.init();

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata modData = mod.getMetadata();
            if (modData.getId().equals("minecraft")) {
                gameVersion = modData.getVersion().getFriendlyString();
                break;
            }
        }

        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER)) {
            log(Level.INFO, "Server initializing...");
            ServerSyncAPI.init();
        }

        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            log(Level.INFO, "Client initializing...");
            dir = Path.of(FabricLoader.getInstance().getGameDir() + "/Quantum");
            if (!dir.toFile().exists()) dir.toFile().mkdir();
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ModsCommand.register(dispatcher);
        });
    }

    public static void log(Level level, String message) {
        LogManager.getLogger().log(level, "[" + MODID + "] " + message);
    }
}