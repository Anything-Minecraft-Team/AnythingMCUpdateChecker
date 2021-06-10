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

    public static void main(String[] args) throws IOException {

        // Check if resources exist in working directory
        // TODO: logging
        boolean configExists = doesFileExist("config.json");
        boolean linksExists = doesFileExist("links.json");

        if (!configExists || !linksExists) {
            System.out.println("Exiting...");
            System.exit(1);
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
