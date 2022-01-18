package mrnavastar.quantum.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.util.ModManager;
import mrnavastar.quantum.util.Settings;
import mrnavastar.quantum.util.datatypes.Mod;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SyncAPI {

    private static final HttpClient client = HttpClient.newHttpClient();

    //@Environment(EnvType.SERVER)
    public static void init() {
        Javalin api = Javalin.create().start(11722);
        JsonObject json = new JsonObject();

        json.addProperty("game_version", Quantum.gameVersion);

        JsonArray requiredMods = new JsonArray();
        Settings.getRequiredMods().forEach((name, version) -> {
            JsonObject mod = new JsonObject();
            mod.addProperty("name", name);
            mod.addProperty("version", version.toString());
            requiredMods.add(mod);
        });
        json.add("required_mods", requiredMods);

        JsonArray recommendedMods = new JsonArray();
        Settings.getRecommendedMods().forEach((name, version) -> {
            JsonObject mod = new JsonObject();
            mod.addProperty("name", name);
            mod.addProperty("version", version.toString());
            recommendedMods.add(mod);
        });
        json.add("recommended_mods", recommendedMods);

        JsonObject status = new JsonObject();
        status.addProperty("status", "online");

        api.get("/mod_data", ctx -> ctx.json(json.toString()));
        api.get("/ping", ctx -> ctx.json(status.toString()));
        api.get("/", ctx -> ctx.html("Quantum - Status: Online \nIf you are on this page, you are probably lost."));
    }

    //@Environment(EnvType.CLIENT)
    public static void sync(String serverAddress) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(serverAddress + "/mod_data"))
                    .build();
            JsonObject json = (JsonObject) JsonParser.parseString(client.send(request, HttpResponse.BodyHandlers.ofString()).body());

            if (!Quantum.gameVersion.equals(json.get("game_version").getAsString())) {
                Quantum.log(Level.ERROR, "Server and client version do not match! Unable to sync mods");
                return;
            }

            //Convert JsonArrays to ArrayLists
            Gson gson = new Gson();
            Mod[] requiredMods = gson.fromJson(json.getAsJsonArray("required_mods"), Mod[].class);
            Mod[] recommendedMods = gson.fromJson(json.getAsJsonArray("recommended_mods"), Mod[].class);

            //Download mods
            for (Mod mod : requiredMods) ModManager.downloadMod(mod);
            for (Mod mod : recommendedMods) ModManager.downloadMod(mod);

            //Prune mods not on the list
            ArrayList<Mod> keepingMods = new ArrayList<>();
            keepingMods.addAll(new ArrayList<>(List.of(requiredMods)));
            keepingMods.addAll(new ArrayList<>(List.of(recommendedMods)));
            ModManager.pruneMods(keepingMods);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
