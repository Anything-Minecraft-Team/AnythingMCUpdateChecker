package org.anythingmc.updatechecker;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.anythingmc.updatechecker.config.Config;
import org.anythingmc.updatechecker.config.Link;
import org.anythingmc.updatechecker.enums.LinkSite;
import org.anythingmc.updatechecker.enums.Status;
import org.anythingmc.updatechecker.enums.StatusManager;
import org.anythingmc.updatechecker.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Getter
public class UpdateChecker {

    public static Gson gson = new Gson();
    public static Link links;
    public static Config config;
    public static Requests request;
    public static WebhookClient webhookClient;

    public static void main(String[] args) throws IOException {

        boolean configExists = Util.doesFileExist("config.json");
        boolean linksExists = Util.doesFileExist("links.json");

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
        int failed = 0;
        int total = 0;
        boolean isRatingOutdated, isVersionOutdated, isStatusOutdated;

        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();

        // checks config to see if an optional avatar url has been passed, and sets embed author
        String avatarUrl;
        if (config.avatarUrl.length() == 0)
            avatarUrl = null;
        else avatarUrl = config.avatarUrl;

        embedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(config.botName, avatarUrl, null));

        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Beginning checking for updates...", null))
                .setColor(0xFFAF33);
        webhookClient.send(embedBuilder.build());

        // loop through all urls
        for (String url : links.urls) {
            System.out.println(url);
            for (Info resourceInfo : request.getMdFile(url)) {
                System.out.println(resourceInfo.url);

                total++;
                isRatingOutdated = isVersionOutdated = isStatusOutdated = false;

                Optional<JsonObject> optionalObject = Optional.empty();

                LinkSite site = Util.getSiteType(resourceInfo.url);

                if (site == null) continue;

                if (site == LinkSite.SPIGOT) optionalObject = request.getSpigotPluginInfo(resourceInfo.url);
                else if (site == LinkSite.GITHUB) continue; //TODO check for updates on github repo
                else if (site == LinkSite.POLYMART) continue; //TODO find polymart API
                else if (site == LinkSite.MCMARKET) continue; //TODO find polymart API
                else if (site == LinkSite.DISCORD) continue;
                else if (site == LinkSite.OTHER) continue;

                // checks to see if the request was successful by checking if the optional object is empty or not
                if (optionalObject.isEmpty()) {

                    if(resourceInfo.url == null) continue;

                    embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Check failed", resourceInfo.url))
                            .setDescription(String.format("Could not check for updates for [this](%s) project, an error has occurred", resourceInfo.url))
                            .setColor(0xFF0000);
                    //webhookClient.send(embedBuilder.build());
                    failed++;
                    System.out.println("Request failed for the url: " + resourceInfo.url);

                    continue;
                }

                JsonObject object = optionalObject.get();

                WebhookEmbedBuilder resourceEmbedBuilder = new WebhookEmbedBuilder();

                JsonArray versions = object.get("testedVersions").getAsJsonArray();
                String rating = object.get("rating").getAsJsonObject().get("average").getAsString();
                rating = rating.substring(0, Math.min(rating.length(), 4));
                rating = rating.contains(".") ? rating.replaceAll("0*$", "").replaceAll("\\.$", "") : rating;

                long lastUpdated = object.get("updateDate").getAsInt();

                String oldVersions = resourceInfo.versions;
                String oldRating = resourceInfo.rating;
                Status oldStatus = resourceInfo.status;

                if (!oldRating.equals(rating)) {
                    isRatingOutdated = true;
                    resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Rating", "Old: " + oldRating + "\nNew: " + rating));
                }

                if (versions.size() == 1) {
                    if (!versions.get(0).getAsString().equals(oldVersions)) {
                        isVersionOutdated = true;
                        resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Version", "Old: " + oldVersions + "\nNew: " + versions.get(0).getAsString()));
                    }

                } else if (versions.size() == 2) {
                    int version1 = Util.getMainVersion(versions.get(0).getAsString());
                    int version2 = Util.getMainVersion(versions.get(1).getAsString());
                    String versionsTogether = version1 + "-" + version2;
                    if (!versionsTogether.equals(oldVersions) && (version1++) == version2) {
                        isVersionOutdated = true;
                        resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Version", "Old: " + oldVersions + "\nNew: " + versions.get(0).getAsString() + "-" + versions.get(1).getAsString()));
                    }
                }

                StatusManager statusManager = new StatusManager(lastUpdated);

                if(oldStatus == Status.PRICE) System.out.println("Skip");
                else if(statusManager.isOutdated(Status.DISCONTINUED) && oldStatus != Status.DISCONTINUED) {
                    isStatusOutdated = true;
                    resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Status", "Old: " + oldStatus.getStatus() + "\nNew: Discontinued"));
                } else if (statusManager.isOutdated(Status.INACTIVE) && oldStatus != Status.INACTIVE) {
                    isStatusOutdated = true;
                    resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Status", "Old: " + oldStatus.getStatus() + "\nNew: Currently Inactive"));
                } else if (statusManager.isOutdated(Status.ACTIVE) && oldStatus != Status.ACTIVE) {
                    isStatusOutdated = true;
                    resourceEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "Status", "Old: " + oldStatus.getStatus() + "\nNew: Active"));
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
            }
        }

        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Finished checking for updates...", null))
                .setDescription(String.format("Up to date: `%d`\nOut of date: `%d`\nFailed/Skipped: `%d`\nTotal: %d", upToDate, outOfDate, failed, total))
                .setColor(0x2DEE12);
        webhookClient.send(embedBuilder.build());

        System.out.println("DONE");
    }
}
