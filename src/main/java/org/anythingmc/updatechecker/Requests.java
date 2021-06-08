package org.anythingmc.updatechecker;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.anythingmc.updatechecker.UpdateChecker.gson;

public class Requests {
    public static final String SPIGOT_API_URL = "https://api.spiget.org/v2/resources/";

    @Nullable
    public static PluginInfo getSpigotPluginInfo(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            System.out.println("An invalid resource has been passed");
            return null;
        }
        String resourceCode = url.substring(index);
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(SPIGOT_API_URL + resourceCode).openConnection();
            int responseCode = request.getResponseCode();
            if (responseCode != 200) {
                InputStream error = request.getErrorStream();
                System.out.println(error);
                return null;
            }
            InputStreamReader reader = new InputStreamReader(request.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                System.out.println(input);
            }
            return gson.fromJson(reader, PluginInfo.class);
        } catch (IOException error) {
            error.printStackTrace();
            return null;
        }
    }
}
