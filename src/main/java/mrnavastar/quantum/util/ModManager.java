package mrnavastar.quantum.util;

import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.api.ModrinthAPI;
import mrnavastar.quantum.util.datatypes.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class ModManager {

    public static final Path modsFolder = Path.of(FabricLoader.getInstance().getGameDir() + "/mods");
    public static final ArrayList<String> versionIds = new ArrayList<>();

    public static void init() {
        File[] mods = modsFolder.toFile().listFiles();
        if (mods != null) {
            for (File mod : mods) {
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                if (versionId != null) versionIds.add(versionId);
            }
        }
    }

    public static void downloadMod(Mod mod) {
        if (!ModManager.versionIds.contains(mod.version)) {
            versionIds.add(mod.version);
            ModrinthAPI.downloadMod(mod.version);
        }
    }

    public static void updateMod(Pair<String, String> versions) {
        File[] mods = modsFolder.toFile().listFiles();
        if (mods != null) {
            for (File mod : mods) {
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                if (versionId != null && versionId.equals(versions.getLeft())) {
                    versionIds.remove(versionId);
                    mod.delete();
                    ModrinthAPI.downloadMod(versions.getRight());
                }
            }
        }
    }

    public static void pruneMods(ArrayList<Mod> keepingMods) {
        File[] mods = modsFolder.toFile().listFiles();
        ArrayList<String> keepingVersionIds = new ArrayList<>();
        for (Mod mod : keepingMods) {
            if (!mod.version.isEmpty()) keepingVersionIds.add(mod.version);
        }

        if (mods != null) {
            for (File mod : mods) {
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                if (versionId != null  && !keepingVersionIds.contains(versionId)) {
                    Quantum.log(Level.INFO, "Removing: " + mod.getName());
                    versionIds.remove(versionId);
                    mod.delete();
                }
            }
        }
    }
}
