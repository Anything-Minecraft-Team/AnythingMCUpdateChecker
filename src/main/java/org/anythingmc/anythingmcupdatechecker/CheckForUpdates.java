package org.anythingmc.anythingmcupdatechecker;

import com.google.gson.JsonElement;
import org.anythingmc.anythingmcupdatechecker.data.Data;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CheckForUpdates {

    public Data data = new Data();

    public CheckForUpdates(){
        int upToDate = 0, outOfDate = 0;
        String url = data.getWebhook();

        try {
            DiscordWebhook webhook = new DiscordWebhook(url);
            webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/837476686978482207/848859250489819156/Untitled.png");
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Update Checker")
                    .setDescription("Starting update checking...")
                    .setColor(Color.ORANGE));
            webhook.execute();
        } catch (IOException e){
            e.printStackTrace();
        }

        List<String> spigotLinks = data.getLinks(0);
        for(String link : spigotLinks){
            boolean isOutOfDate = false;
            System.out.println(link);
            JsonElement element = data.getData(link.replace("https://www.spigotmc.org/resources/", ""));
            System.out.println("Name: " + data.getPluginName(element) + "\nSupported Versions: " + data.getPluginVersions(element) + "\nVersion ID: " + data.getVersion(element) + "\nSkript: " + data.isSkript(element));
            if(!isOutOfDate) {
                upToDate += 1;
            } else {
                outOfDate += 1;
                try {
                    DiscordWebhook webhook = new DiscordWebhook(url);
                    webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/837476686978482207/848859250489819156/Untitled.png");
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(data.getPluginName(element))
                            .addField("Out Of Date", "[" + data.getPluginName(element) + "](" + link.replaceAll("\"", "") + ")", false)
                            .setColor(Color.RED));
                    if(!isOutOfDate)
                        webhook.execute();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        try {
            DiscordWebhook webhook = new DiscordWebhook(url);
            webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/837476686978482207/848859250489819156/Untitled.png");
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Update Checker")
                    .addField("Up To Date Plugins: ", String.valueOf(upToDate), false)
                    .addField("Out Of Date Plugins: ", String.valueOf(outOfDate), false)
                    .setColor(Color.GREEN));
            webhook.execute();
        } catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("offline");
    }
}