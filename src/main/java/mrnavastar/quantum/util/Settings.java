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

    public static void init() {
        if (!file.exists()) {
            try {
                class Template {
                    final HashMap<String, Object> General = new HashMap<>();
                    final HashMap<String, String> ServerSideMods = new HashMap<>();
                    final HashMap<String, String> RequiredMods = new HashMap<>();
                    final HashMap<String, String> RecommendedMods = new HashMap<>();

                    public Template() {
                        ServerSideMods.put("example_mod", "version_id");
                        RequiredMods.put("example_mod", "version_id");
                        RecommendedMods.put("example_mod", "version_id");
                    }
                }

                TomlWriter writer = new TomlWriter.Builder()
                        .indentValuesBy(2)
                        .indentTablesBy(4)
                        .padArrayDelimitersBy(3)
                        .build();
                writer.write(new Template(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toml toml = new Toml().read(file);
            serverSideMods = toml.getTable("ServerSideMods").toMap();
            requiredMods = toml.getTable("RequiredMods").toMap();
            recommendedMods = toml.getTable("RecommendedMods").toMap();

            serverSideMods.remove("example_mod");
            requiredMods.remove("example_mod");
            recommendedMods.remove("example_mod");
        }
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
}
