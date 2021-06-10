package org.anythingmc.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateChecker {

    public static Gson gson = new Gson();
    public static Link[] links;
    public static Config config;
    public static Requests request;

    public static void main(String[] args) throws IOException {

        // Check if links.json exists
        // TODO: logging
        InputStream stream;
        if (!Files.exists(Path.of("links.json"))) {
            System.out.println("Could not find links.json, generating one for you...");
            stream = UpdateChecker.class.getResourceAsStream("/links.json");
            assert stream != null;
            Files.copy(stream, Path.of("links.json"));
            System.out.println("Exiting...");
            System.exit(0);
        }

        // Check if config.json exists
        if (!Files.exists(Path.of("config.json"))) {
            System.out.println("Could not find config.json, generating one for you...");
            stream = UpdateChecker.class.getResourceAsStream("/config.json");
            assert stream != null;
            Files.copy(stream, Path.of("config.json"));
        }

        // Parse the links
        String data = Files.readString(Path.of("links.json"));
        links = gson.fromJson(data, Link[].class);

        // Parse the config
        data = Files.readString(Path.of("config.json"));
        config = gson.fromJson(data, Config.class);

        // Initialise request object
        request = new Requests();

        // Timer task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                checkUpdates();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, config.checkInterval * 1000L);
    }

    private static void checkUpdates() {
        for (String url : links[0].urls) {
            Optional<JsonObject> optionalObject = request.getSpigotPluginInfo(url);
            if (optionalObject.isPresent()) {
                JsonObject object = optionalObject.get();
                System.out.println(object.get("name").getAsString());
            } else {
                System.out.println("Request failed for the url: " + url);
            }
        }
    }
}
