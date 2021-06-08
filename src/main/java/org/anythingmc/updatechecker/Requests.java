package org.anythingmc.updatechecker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

public class Requests {
    public static final String SPIGOT_API_URL = "https://api.spiget.org/v2/resources/";
    public static final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Nullable
    public static JsonObject getSpigotPluginInfo(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            System.out.println("An invalid resource has been passed");
            return null;
        }
        String resourceCode = url.substring(index);

        try {
            HttpGet get = new HttpGet(SPIGOT_API_URL + resourceCode);
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.out.println(response.getStatusLine().getReasonPhrase());
            }
            JsonElement element = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent()));
            return element.getAsJsonObject();
        } catch (IOException error) {
            error.printStackTrace();
            return null;
        }
    }
}
