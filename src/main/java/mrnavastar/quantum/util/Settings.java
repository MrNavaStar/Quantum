package mrnavastar.quantum.util;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import mrnavastar.quantum.Quantum;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Settings {

    private static final File file = new File(FabricLoader.getInstance().getConfigDir() + "/" + Quantum.MODID + ".toml");
    private static Map<String, Object> serverSideMods = new HashMap<>();
    private static Map<String, Object> requiredMods = new HashMap<>();
    private static Map<String, Object> recommendedMods = new HashMap<>();
    private static Map<String, Object> whitelist = new HashMap<>();

    static class Template {
        final Map<String, Object> ServerSideMods = serverSideMods;
        final Map<String, Object> RequiredMods = requiredMods;
        final Map<String, Object> RecommendedMods = recommendedMods;
    }

    private static void saveFile(Template template) {
        try {
            TomlWriter writer = new TomlWriter.Builder()
                    .indentValuesBy(2)
                    .indentTablesBy(4)
                    .padArrayDelimitersBy(3)
                    .build();
            writer.write(template, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfigState() {
        saveFile(new Template());
    }

    public static void init() {
        if (!file.exists()) {
            Template template = new Template();
            template.ServerSideMods.put("example_mod", "version_id");
            template.RequiredMods.put("example_mod", "version_id");
            template.RecommendedMods.put("example_mod", "version_id");
            saveFile(template);
        } else {
            Toml toml = new Toml().read(file);
            serverSideMods = toml.getTable("ServerSideMods").toMap();
            requiredMods = toml.getTable("RequiredMods").toMap();
            recommendedMods = toml.getTable("RecommendedMods").toMap();

            serverSideMods.remove("example_mod");
            requiredMods.remove("example_mod");
            recommendedMods.remove("example_mod");
        }

        Toml toml = new Toml().read(Settings.class.getClassLoader().getResourceAsStream("modwhitelist.toml"));
        whitelist = toml.getTable("Whitelist").toMap();
    }

    public static Map<String, Object> getServerSideMods() {
        return serverSideMods;
    }

    public static Map<String, Object> getRequiredMods() {
        return requiredMods;
    }

    public static Map<String, Object> getRecommendedMods() {
        return recommendedMods;
    }

    public static Map<String, Object> getWhitelist() {
        return whitelist;
    }
}
