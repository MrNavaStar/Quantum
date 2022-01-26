package mrnavastar.quantum.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.systems.RenderSystem;
import mrnavastar.quantum.Quantum;
import mrnavastar.quantum.util.FileHelpers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class Launcher {

    public static void reboot() {
        try {
            List<String> jarArgs = new ArrayList<>(List.of(FabricLoader.getInstance().getLaunchArguments(false)));
            List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();

            String jvmPath = null;
            Optional<String> cmdInfo = ProcessHandle.current().info().command();
            if (cmdInfo.isPresent()) jvmPath = cmdInfo.get();

            //Build command and run
            if (jvmPath != null) {
                StringBuilder cmd = new StringBuilder();
                cmd.append(jvmPath);
                jvmArgs.forEach(arg -> cmd.append(" ").append(arg.replaceAll(" ", "")));

                cmd.append(" -cp ");
                cmd.append(System.getProperty("java.class.path"));
                cmd.append(" net.fabricmc.loader.impl.launch.knot.KnotClient");
                jarArgs.forEach(arg -> cmd.append(" ").append(arg));

                System.out.println(cmd);

                ProcessBuilder builder = new ProcessBuilder();
                builder.command(String.valueOf(cmd));
                builder.directory(FabricLoader.getInstance().getGameDir().toFile());
                builder.start();

                //Runtime.getRuntime().exec(cmd.toString());
                MinecraftClient.getInstance().scheduleStop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}