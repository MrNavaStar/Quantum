package mrnavastar.quantum.services;

import mrnavastar.quantum.api.ModrinthAPI;
import mrnavastar.quantum.util.FileHelpers;
import mrnavastar.quantum.util.Settings;
import mrnavastar.quantum.util.datatypes.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class ModManager {

    public static final Path modsFolder = Path.of(FabricLoader.getInstance().getGameDir() + "/mods");
    public static final HashMap<String, Mod> downloadedMods = new HashMap<>();

    public static void init() {
        File[] mods = modsFolder.toFile().listFiles();
        if (mods != null) {
            for (File mod : mods) {
                String name = FileHelpers.readMetaData(mod.getPath(), "name");
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                String type = FileHelpers.readMetaData(mod.getPath(), "type");
                if (name != null && versionId != null && type != null) downloadedMods.put(versionId, new Mod(name, versionId, type));
            }
        }
    }

    public static boolean isDownloaded(String versionId) {
        return downloadedMods.containsKey(versionId);
    }

    public static void downloadMod(Mod mod) {
        downloadedMods.put(mod.getVersionId(), mod);
        ModrinthAPI.downloadMod(mod);
    }

    public static void removeMod(Mod modData) {
        File[] mods = modsFolder.toFile().listFiles();
        if (mods != null) {
            for (File modFile : mods) {
                String versionId = FileHelpers.readMetaData(modFile.getPath(), "versionId");
                if (versionId != null && versionId.equals(modData.getVersionId())) {
                    downloadedMods.remove(versionId);
                    modFile.delete();

                    if (modData.getType().equals("server")) Settings.getServerSideMods().remove(modData.getName());
                    if (modData.getType().equals("required")) Settings.getRequiredMods().remove(modData.getName());
                    if (modData.getType().equals("recommended")) Settings.getRecommendedMods().remove(modData.getName());
                    Settings.saveConfigState();
                }
            }
        }
    }

    public static void updateMod(String name, Pair<String, String> versions) {
        File[] mods = modsFolder.toFile().listFiles();
        if (mods != null) {
            for (File mod : mods) {
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                if (versionId != null && versionId.equals(versions.getLeft())) {
                    Mod modData = downloadedMods.remove(versionId);
                    mod.delete();
                    downloadMod(new Mod(name, versions.getRight(), modData.getType()));

                    if (modData.getType().equals("server")) Settings.getServerSideMods().put(name, versions.getRight());
                    if (modData.getType().equals("required")) Settings.getRequiredMods().put(name, versions.getRight());
                    if (modData.getType().equals("recommended")) Settings.getRecommendedMods().put(name, versions.getRight());
                    Settings.saveConfigState();
                }
            }
        }
    }

    public static ArrayList<Mod> pruneMods(ArrayList<Mod> keepingMods) {
        File[] mods = modsFolder.toFile().listFiles();
        ArrayList<Mod> removedMods = new ArrayList<>();
        ArrayList<String> keepingVersionIds = new ArrayList<>();
        for (Mod mod : keepingMods) {
            if (!mod.getVersionId().isEmpty()) keepingVersionIds.add(mod.getVersionId());
        }

        if (mods != null) {
            for (File mod : mods) {
                String versionId = FileHelpers.readMetaData(mod.getPath(), "versionId");
                if (versionId != null  && !keepingVersionIds.contains(versionId)) {
                    removedMods.add(downloadedMods.remove(versionId));
                    mod.delete();
                }
            }
        }
        return removedMods;
    }
}