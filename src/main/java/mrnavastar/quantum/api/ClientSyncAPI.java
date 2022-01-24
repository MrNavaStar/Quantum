package mrnavastar.quantum.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.client.SyncScreen;
import mrnavastar.quantum.services.ModManager;
import mrnavastar.quantum.util.datatypes.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ClientSyncAPI {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static boolean syncServerOnline(String serverAddress) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(serverAddress + "/ping"))
                    .build();
            JsonObject json = (JsonObject) JsonParser.parseString(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
            if (json.get("status").getAsString().equals("online")) return true;
        } catch (URISyntaxException | InterruptedException | IOException ignore) {}
        return false;
    }

    public static void sync(String serverAddress, MinecraftClient mclient) {
        if (!syncServerOnline(serverAddress)) {
            Quantum.log(Level.ERROR, "Sync server is offline!");
            return;
        }

        mclient.setScreen(new SyncScreen(""));

        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("game_version", Quantum.gameVersion);

        JsonArray jsonMods = new JsonArray();
        for (Mod modData : ModManager.downloadedMods.values()) {
            JsonObject mod = new JsonObject();
            mod.addProperty("name", modData.getName());
            mod.addProperty("version", modData.getVersionId());
            mod.addProperty("type", modData.getType());
            jsonMods.add(mod);
        }
        jsonRequest.add("mods", jsonMods);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .uri(new URI(serverAddress + "/mods"))
                    .build();
            JsonObject json = (JsonObject) JsonParser.parseString(client.send(request, HttpResponse.BodyHandlers.ofString()).body());

            if (!json.get("status").getAsString().equals("good")) {
                Quantum.log(Level.ERROR, json.get("error").getAsString());
                return;
            }

            ArrayList<Mod> keepingMods = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("mods")) {
                JsonObject jsonMod = element.getAsJsonObject();
                Mod mod = new Mod(jsonMod.get("name").getAsString(), jsonMod.get("version").getAsString(), jsonMod.get("type").getAsString());
                String action = jsonMod.get("action").getAsString();

                if (action.equals("download")) {
                    ModManager.downloadMod(mod);
                    mclient.setScreen(new SyncScreen("Downloaded: " + mod));
                    Quantum.log(Level.INFO, "Downloaded: " + mod);
                }
                if (action.equals("update")) {
                    for (Mod downloadedMod : ModManager.downloadedMods.values()) {
                        if (downloadedMod.getName().equals(mod.getName())) {
                            ModManager.updateMod(mod.getName(), new Pair<>(downloadedMod.getVersionId(), mod.getVersionId()));
                            mclient.setScreen(new SyncScreen("Updated: " + mod));
                            Quantum.log(Level.INFO, "Updated: " + mod);
                            break;
                        }
                    }
                }
                keepingMods.add(mod);
            }
            ArrayList<Mod> removedMods = ModManager.pruneMods(keepingMods);
            removedMods.forEach(mod -> Quantum.log(Level.INFO, "Removed: " + mod));
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
