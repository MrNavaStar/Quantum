package mrnavastar.quantum.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.util.Settings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executors;

@Environment(EnvType.SERVER)
public class ServerSyncAPI {

    public static void init() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(11722), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.createContext("/mods", new ModsHttpHandler());
            server.createContext("/ping", new PingHttpHandler());
            server.start();
            Quantum.log(Level.INFO, "Internal http server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PingHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                JsonObject json = new JsonObject();
                json.addProperty("status", "online");

                String response = json.toString();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();
            }
        }
    }

    static class ModsHttpHandler implements HttpHandler {

        private void parseMod(ArrayList<String> clientModNames, ArrayList<String> clientModVersionIds, JsonArray mods, String name, String version, String type) {
            JsonObject mod = new JsonObject();
            mod.addProperty("name", name);
            mod.addProperty("version", version);
            mod.addProperty("type", type);

            if (clientModNames.contains(name)) {
                if (!clientModVersionIds.contains(version)) mod.addProperty("action", "update");
            } else mod.addProperty("action", "download");
            mods.add(mod);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("POST")) {
                Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                JsonObject request = new Gson().fromJson(reader, JsonObject.class);

                ArrayList<String> clientModNames = new ArrayList<>();
                ArrayList<String> clientModVersionIds = new ArrayList<>();
                for (JsonElement element : request.getAsJsonArray("mods")) {
                    JsonObject mod = element.getAsJsonObject();
                    clientModNames.add(mod.get("name").getAsString());
                    clientModVersionIds.add(mod.get("version").getAsString());
                }

                JsonObject json = new JsonObject();
                if (request.get("game_version").getAsString().equals(Quantum.gameVersion)) {
                    json.addProperty("status", "good");
                    JsonArray mods = new JsonArray();
                    Settings.getRequiredMods().forEach((name, version) -> parseMod(clientModNames, clientModVersionIds, mods, name, version.toString(), "required"));
                    Settings.getRecommendedMods().forEach((name, version) -> parseMod(clientModNames, clientModVersionIds, mods, name, version.toString(), "recommended"));
                    json.add("mods", mods);
                } else {
                    json.addProperty("status", "failed");
                    json.addProperty("error", "Server and client game version do not match!");
                }

                String response = json.toString();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();
            }
        }
    }
}