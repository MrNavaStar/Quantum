package mrnavastar.quantum;

import mrnavastar.quantum.api.SyncAPI;
import mrnavastar.quantum.commands.ConfigCommand;
import mrnavastar.quantum.commands.ModsCommand;
import mrnavastar.quantum.util.FileHelpers;
import mrnavastar.quantum.util.ModManager;
import mrnavastar.quantum.util.Settings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Quantum implements ModInitializer {

    public static final String MODID = "Quantum";
    public static String gameVersion;

    @Override
    public void onInitialize() {
        if (!FileHelpers.supportedFileSystem()) {
            log(Level.ERROR, "Quantum failed to start! Unsupported FileSystem!");
            return;
        }

        Settings.init();

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata modData = mod.getMetadata();
            if (modData.getId().equals("minecraft")) {
                gameVersion = modData.getVersion().getFriendlyString();
                break;
            }
        }

        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER)) {
            log(Level.INFO, "Server initializing...");
            SyncAPI.init();

            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                ModsCommand.register(dispatcher);
                ConfigCommand.register(dispatcher);
            });
        }

        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            log(Level.INFO, "Client initializing...");
            ModManager.init();
            //SyncAPI.sync("http://localhost:11722");
        }

        //ModrinthAPI.downloadMod("U9Cb1VzA");
    }

    public static void log(Level level, String message) {
        LogManager.getLogger().log(level, "[" + MODID + "] " + message);
    }
}