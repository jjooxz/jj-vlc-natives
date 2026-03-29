package me.jojoquis.vlc;

import com.sun.jna.NativeLibrary;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

public class VlcInitializer {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "jj-vlc");
            if (!tempDir.exists()) tempDir.mkdirs();

            // DLLs principais
            extract("/lib/vlc/libvlc.dll", tempDir);
            extract("/lib/vlc/libvlccore.dll", tempDir);
            extract("/lib/vlc/axvlc.dll", tempDir);
            extract("/lib/vlc/npvlc.dll", tempDir);

            // Plugins (PASTA COMPLETA)
            File pluginsDir = new File(tempDir, "plugins");
            if (!pluginsDir.exists()) {
                copyResourceDirectory("/lib/vlc/plugins", pluginsDir);
            }

            // Config VLC
            System.setProperty("jna.nosys", "true");
            NativeLibrary.addSearchPath("libvlc", tempDir.getAbsolutePath());
            System.setProperty("VLC_PLUGIN_PATH", pluginsDir.getAbsolutePath());

            initialized = true;

            System.out.println("[JJ-VLC] OK -> " + tempDir);

        } catch (Exception e) {
            throw new RuntimeException("Failed to init VLC", e);
        }
    }

    private static void extract(String resource, File dir) throws IOException {
        File out = new File(dir, new File(resource).getName());
        if (out.exists()) return;

        try (InputStream in = VlcInitializer.class.getResourceAsStream(resource)) {
            if (in == null) throw new FileNotFoundException(resource);
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyResourceDirectory(String resourcePath, File destDir) throws Exception {
        destDir.mkdirs();

        URI uri = VlcInitializer.class.getResource(resourcePath).toURI();

        if (uri.getScheme().equals("jar")) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path path = fs.getPath(resourcePath);
                copyFolder(path, destDir.toPath());
            }
        } else {
            Path path = Paths.get(uri);
            copyFolder(path, destDir.toPath());
        }
    }

    private static void copyFolder(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path dest = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}