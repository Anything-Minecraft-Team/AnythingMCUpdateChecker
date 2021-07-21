package org.anythingmc.updatechecker;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class UpdateChecker {

    public static Gson gson = new Gson();
    public static Link links;
    public static Config config;
    public static Requests request;
    public static WebhookClient webhookClient;

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

        boolean configExists = doesFileExist("config.json");
        boolean linksExists = doesFileExist("links.json");

        if (!configExists || !linksExists) {
            System.out.println("Exiting...");
            System.exit(1);
        }

        // Parse the links
        String data = Files.readString(Path.of("links.json"));
        links = gson.fromJson(data, Link.class);

        // Parse the config
        data = Files.readString(Path.of("config.json"));
        config = gson.fromJson(data, Config.class);

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
        boolean isRatingOutdated, isVersionOutdated, isStatusOutdated;

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
        //webhookClient.send(embedBuilder.build());

        // loop through all urls
        for (String url : links.urls) {
            for (OldInfo resourceInfo : request.getMdFile(url)) {
                isRatingOutdated = false;
                isVersionOutdated = false;
                isStatusOutdated = false;

                Optional<JsonObject> optionalObject = Optional.empty();

                LinkSite site = getSiteType(resourceInfo.url);

                if (site == null)
                    continue;

                if (site == LinkSite.SPIGOT) {
                    optionalObject = request.getSpigotPluginInfo(resourceInfo.url);
                } else if (site == LinkSite.GITHUB) {
                    continue;
                    //TODO check for updates on github repo
                } else if (site == LinkSite.POLYMART) {
                    continue;
                    //TODO find polymart API
                } else if (site == LinkSite.MCMARKET) {
                    continue;
                    //TODO find polymart API
                } else if (site == LinkSite.DISCORD) {
                    continue;
                } else if (site == LinkSite.OTHER) {
                    continue;
                }

                // checks to see if the request was successful by checking if the optional object is empty or not
                if (optionalObject.isPresent()) {
                    JsonObject object = optionalObject.get();

                    WebhookEmbedBuilder resourceEmbedBuilder = new WebhookEmbedBuilder();

                    JsonArray versions = object.get("testedVersions").getAsJsonArray();
                    String rating = object.get("rating").getAsJsonObject().get("average").getAsString();
                    rating = rating.substring(0, Math.min(rating.length(), 4));

                    String oldVersions = resourceInfo.versions;
                    String oldRating = resourceInfo.rating;
                    
                    if(!oldRating.equals(rating)) {
                        isRatingOutdated = true;
                        resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Rating", "Old: " + oldRating + "\nNew: " + rating));
                    }

                    if (versions.size() == 1){
                            if(!versions.get(0).getAsString().equals(oldVersions)) {
                                isVersionOutdated = true;
                                resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Version", "Old: " + oldVersions + "\nNew: " + versions.get(0).getAsString()));
                            }

                    } else if(versions.size() == 2){
                        String versionsTogether = versions.get(0).getAsString() + "-" + versions.get(1).getAsString();
                        if(!versionsTogether.equals(oldVersions)) {
                            isVersionOutdated = true;
                            resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Version", "Old: " + oldVersions + "\nNew: " + versions.get(0).getAsString() + "-" + versions.get(1).getAsString()));
                        }
                    }

                    if (!isRatingOutdated && !isVersionOutdated && !isStatusOutdated) {
                        upToDate++;
                    } else {
                        String urlId = resourceInfo.url;
                        if (urlId.endsWith("/"))
                            urlId = urlId.substring(0, urlId.length() - 1);
                        int index = urlId.lastIndexOf("/");

                        String resourceCode = urlId.substring(index);
                        resourceCode = resourceCode.replaceAll("/", "");

                        outOfDate++;
                        String name = object.get("name").getAsString();
                        resourceEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(name, resourceInfo.url))
                                .setColor(0x00FFB9)
                                .setDescription(String.format("`%s` is out of date, click [here](%s)", name, resourceInfo.url))
                                .setFooter(new WebhookEmbed.EmbedFooter(resourceCode, null));
                        webhookClient.send(resourceEmbedBuilder.build());
                    }
                } else {  // invoked when an error has occurred in the request
                    embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Check failed", resourceInfo.url))
                            .setDescription(String.format("Could not check for updates for [this](%s) project, an error has occurred", resourceInfo.url))
                            .setColor(0xFF0000);
                    //webhookClient.send(embedBuilder.build());
                    System.out.println("Request failed for the url: " + resourceInfo.url);
                }
            }
        }

        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Finished checking for updates...", null))
                .setDescription(String.format("Up to date: `%d`\nOut of date: `%d`", upToDate, outOfDate))
                .setColor(0x2DEE12);
        webhookClient.send(embedBuilder.build());

        System.out.println("DONE");
    }

    private static LinkSite getSiteType(String url) {
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
}
