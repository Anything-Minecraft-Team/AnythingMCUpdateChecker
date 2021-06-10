package org.anythingmc.updatechecker;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
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
    public static WebhookClient webhookClient;
    public static Database database = new Database();

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

        // check if webhook url exists in config
        if (config.webhookUrl == null || config.webhookUrl.length() == 0) {
            System.out.println("No webhook url passed in config, exiting...");
            System.exit(1);
        }

        // Initialise webhook client
        try {
            webhookClient = WebhookClient.withUrl(config.webhookUrl);
        } catch (IllegalArgumentException error) {
            System.out.println("An invalid webhook url has been passed in the config, exiting...");
            System.exit(1);
        }

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
        int upToDate = 0;
        int outOfDate = 0;
        boolean isOutdated;

        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();

        // checks config to see if an optional avatar url has been passed, and sets embed author
        String avatarUrl;
        if (config.avatarUrl.length() == 0) {
            avatarUrl = null;
        } else {
            avatarUrl = config.avatarUrl;
        }
        embedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(config.botName, avatarUrl, null));

        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Beginning checking for updates...", null))
                .setColor(0xFFAF33);
        webhookClient.send(embedBuilder.build());

        // loop through all urls within the spigot list
        for (String url : links[0].urls) {
            isOutdated = false;
            Optional<JsonObject> optionalObject = request.getSpigotPluginInfo(url);

            // checks to see if the request was successful by checking if the optional object is empty or not
            if (optionalObject.isPresent()) {
                JsonObject object = optionalObject.get();
                // TODO: Version checking

                if (!isOutdated) {
                    upToDate++;
                } else {
                    outOfDate++;
                    String name = object.get("name").getAsString();
                    embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(name, url))
                            .setColor(0x00FFB9)
                            .setDescription(String.format("`%s` is out of date, click [here](%s)", name, url));
                    webhookClient.send(embedBuilder.build());
                }
            } else {  // invoked when an error has occurred in the request
                embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Check failed", url))
                        .setDescription(String.format("Could not check for updates for [this](%s) project, an error has occurred", url))
                        .setColor(0xFF0000);
                webhookClient.send(embedBuilder.build());
                System.out.println("Request failed for the url: " + url);
            }
        }
        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Finished checking for updates...", null))
                .setDescription(String.format("Up to date: `%d`\nOut of date: `%d`", upToDate, outOfDate))
                .setColor(0x2DEE12);
        webhookClient.send(embedBuilder.build());
    }
}
