package mrnavastar.quantum.services;

import mrnavastar.quantum.util.FileHelpers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.io.FileInputStream;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class Launcher {

    private static void rebootUnix() {
        try {
            FileInputStream reader = new FileInputStream(String.format("/proc/%s/cmdline", ProcessHandle.current().pid()));
            String cmd = FileHelpers.readFromInputStream(reader);
            Runtime.getRuntime().exec(cmd);
            MinecraftClient.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rebootWindows() {

    }

    public static void reboot() {

    }
}
