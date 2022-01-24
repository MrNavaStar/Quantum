package mrnavastar.quantum.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.util.FileHelpers;
import mrnavastar.quantum.services.ModManager;
import mrnavastar.quantum.util.Settings;
import mrnavastar.quantum.util.datatypes.Mod;
import net.minecraft.util.Pair;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ModrinthAPI {

    private static final String MODRINTH_URL = "https://api.modrinth.com/api/v1";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void downloadMod(Mod mod) {
        try {
            //Make Request
            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(MODRINTH_URL + "/version/" + mod.getVersionId())).build();
            JsonObject json = (JsonObject) JsonParser.parseString(client.send(request, HttpResponse.BodyHandlers.ofString()).body());

            for (JsonElement element2 : json.getAsJsonArray("files")) {
                JsonObject file = element2.getAsJsonObject();
                String filename = file.get("filename").getAsString();

                //Download file + write metadata
                String filePath = ModManager.modsFolder + "/" + filename;
                FileHelpers.downloadFile(filePath, file.get("url").getAsString());
                FileHelpers.writeMetaData(filePath, "name", mod.getName());
                FileHelpers.writeMetaData(filePath, "versionId", mod.getVersionId());
                FileHelpers.writeMetaData(filePath, "type", mod.getType());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getUpdate(String versionId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            //Make request
            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(MODRINTH_URL + "/version/" + versionId)).build();
            JsonObject json = (JsonObject) JsonParser.parseString(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
            HttpRequest request2 = HttpRequest.newBuilder().GET().uri(URI.create(MODRINTH_URL + "/mod/" + json.get("mod_id").getAsString() + "/version")).build();
            JsonArray json2 = (JsonArray) JsonParser.parseString(client.send(request2, HttpResponse.BodyHandlers.ofString()).body());

            //Look for latest release
            for (JsonElement element : json2) {
                JsonObject version = element.getAsJsonObject();
                String newVersionId = version.get("id").getAsString();

                String name = version.get("name").getAsString().toLowerCase();
                String versionNumber = version.get("version_number").getAsString().toLowerCase();

                Date date = format.parse(json.get("date_published").getAsString());
                Date newDate = format.parse(version.get("date_published").getAsString());

                if (!newVersionId.equals(versionId) && date.compareTo(newDate) < 0 && !name.contains("forge") && !versionNumber.contains("forge")) {
                    for (JsonElement element2 : version.getAsJsonArray("game_versions")) {
                        if (element2.getAsString().equals(Quantum.gameVersion)) {
                            return newVersionId;
                        }
                    }
                }
            }

        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, Pair<String, String>> getUpdates() {
        HashMap<String, Pair<String, String>> updates = new HashMap<>();
        Settings.getServerSideMods().forEach((name, versionId) -> {
            String newVersionId = getUpdate(versionId.toString());
            if (newVersionId != null) updates.put(name, new Pair<>(versionId.toString(), newVersionId));
        });
        Settings.getRequiredMods().forEach((name, versionId) -> {
            String newVersionId = getUpdate(versionId.toString());
            if (newVersionId != null) updates.put(name, new Pair<>(versionId.toString(), newVersionId));
        });
        Settings.getRecommendedMods().forEach((name, versionId) -> {
            String newVersionId = getUpdate(versionId.toString());
            if (newVersionId != null) updates.put(name, new Pair<>(versionId.toString(), newVersionId));
        });
        return updates;
    }
}