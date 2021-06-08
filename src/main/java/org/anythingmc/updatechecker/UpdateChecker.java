package org.anythingmc.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateChecker {

    public static Gson gson = new Gson();
    public static Link[] links;
    public static Config config;

    public static void main(String[] args) throws IOException {

        // Check if links.json exists
        // TODO: logging
        if (!Files.exists(Path.of("links.json"))) {
            System.out.println("Could not find links.json, generating one for you...");
            InputStream stream = UpdateChecker.class.getResourceAsStream("/links.json");
            assert stream != null;
            Files.copy(stream, Path.of("links.json"));
            System.out.println("Exiting...");
            System.exit(0);
        }

        // Check if config.json exists
        if (!Files.exists(Path.of("config.json"))) {
            System.out.println("Could not find config.json, generating one for you...");
            InputStream stream = UpdateChecker.class.getResourceAsStream("/config.json");
            assert stream != null;
            Files.copy(stream, Path.of("config.json"));
        }

        // Parse the links
        String data = Files.readString(Path.of("links.json"));
        links = gson.fromJson(data, Link[].class);

        // Parse the config
        data = Files.readString(Path.of("config.json"));
        config = gson.fromJson(data, Config.class);

        JsonObject object = Requests.getSpigotPluginInfo(links[0].urls[0]);

        // Timer task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, config.checkInterval * 1000L);
    }
}
