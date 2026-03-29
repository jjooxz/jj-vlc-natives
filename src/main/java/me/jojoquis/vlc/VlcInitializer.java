package me.jojoquis.vlc;

import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VlcInitializer {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "jj-vlc");
            if (!tempDir.exists()) tempDir.mkdirs();

            extract("/vlc/libvlc.dll", tempDir);
            extract("/vlc/libvlccore.dll", tempDir);
            extract("/vlc/axvlc.dll", tempDir);
            extract("/vlc/npvlc.dll", tempDir);

            File pluginsDir = new File(tempDir, "plugins");
            if (!pluginsDir.exists()) {
                unzip("/vlc/plugins.zip", pluginsDir);
            }

            System.setProperty("jna.nosys", "true");
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), tempDir.getAbsolutePath());
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

    private static void unzip(String resource, File dest) throws IOException {
        dest.mkdirs();

        try (InputStream in = VlcInitializer.class.getResourceAsStream(resource);
             ZipInputStream zis = new ZipInputStream(in)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(dest, entry.getName());

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        zis.transferTo(fos);
                    }
                }
            }
        }
    }
}