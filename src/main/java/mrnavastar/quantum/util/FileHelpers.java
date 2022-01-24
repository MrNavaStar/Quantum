package mrnavastar.quantum.util;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public class FileHelpers {

    public static boolean supportedFileSystem() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("user");
    }

    public static void downloadFile(String filePath, String url) {
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            new File(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromInputStream(InputStream inputStream) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultStringBuilder.toString();
    }

    public static void writeMetaData(String filePath, String key, String value) {
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(Path.of(filePath), UserDefinedFileAttributeView.class);
            key = String.format("com.mrnavastar.quantum.%s", key);

            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            view.write(key, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readMetaData(String filePath, String key) {
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(Path.of(filePath), UserDefinedFileAttributeView.class);
            key = String.format("com.mrnavastar.quantum.%s", key);

            ByteBuffer buffer = ByteBuffer.allocate(view.size(key));
            view.read(key, buffer);
            buffer.flip();
            return new String(buffer.array(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
        return null;
    }
}