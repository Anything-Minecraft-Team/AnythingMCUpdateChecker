package org.anythingmc.updatechecker.util;

import org.anythingmc.updatechecker.UpdateChecker;
import org.anythingmc.updatechecker.enums.LinkSite;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {

    public static boolean doesFileExist(String filename) throws IOException {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            System.out.printf("Could not find %s, generating it for you...\n", filename);
            InputStream stream = UpdateChecker.class.getResourceAsStream("/" + filename);
            assert stream != null;
            Files.copy(stream, path);
            return false;
        }
        return true;
    }

    public static LinkSite getSiteType(String url) {
        if (url.contains("spigotmc.org"))
            return LinkSite.SPIGOT;

        if (url.contains("github.com"))
            return LinkSite.GITHUB;

        if (url.contains("mc-market.org"))
            return LinkSite.MCMARKET;

        if (url.contains("polymart.org"))
            return LinkSite.POLYMART;

        if (url.contains("discord"))
            return LinkSite.DISCORD;

        try {
            new URL(url);
            return LinkSite.OTHER;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static int getMainVersion(String version) {
        version = version.replace("1.", "");
        double doubleVersion = Double.parseDouble(version);
        //version = String.valueOf(x);
        return (int) doubleVersion;
    }
}
