package org.anythingmc.anythingmcupdatechecker;

import com.google.gson.JsonElement;
import org.anythingmc.anythingmcupdatechecker.data.Data;
import org.anythingmc.anythingmcupdatechecker.data.DiscordWebhook;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CheckForUpdates {

    public Data data = new Data();

    public CheckForUpdates(){
        int upToDate = 0, outOfDate = 0;

        List<String> spigotLinks = data.getLinks(0);
        for(String link : spigotLinks){
            System.out.println(link);
            JsonElement element = data.getData(link.replace("https://www.spigotmc.org/resources/", ""));
            System.out.println("Name: " + data.getPluginName(element) + "\nSupported Versions: " + data.getPluginVersions(element) + "\nVersion ID: " + data.getVersion(element) + "\nSkript: " + data.isSkript(element));

            try {
                DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/851606277641732137/cxs6EiX_p99WyxX-nKgNW_Zu3MNadum1ejmT5weF6IcKcgwGSy7jqKu1C_nwfA6VwV63");
                webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/837476686978482207/848859250489819156/Untitled.png");
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(data.getPluginName(element))
                        .addField("Supported Versions: ", String.valueOf(data.getPluginVersions(element)), false)
                        .addField("Version ID: ", String.valueOf(data.getVersion(element)), false)
                        .addField("Skript: ", String.valueOf(data.isSkript(element)), false)
                        .setColor(Color.GREEN));
                webhook.execute(); //Handle exception
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}