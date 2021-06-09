package org.anythingmc.updatechecker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import static org.anythingmc.updatechecker.UpdateChecker.config;

public class Requests {
    public static final String SPIGOT_API_URL = "https://api.spiget.org/v2/resources/";
    public HttpClient httpClient;

    public Requests() {
        int timeout = config.requestTimeout * 1000;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public Optional<JsonObject> getSpigotPluginInfo(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            System.out.println("An invalid resource has been passed");
            return Optional.empty();
        }
        String resourceCode = url.substring(index);

        HttpGet get = new HttpGet(SPIGOT_API_URL + resourceCode);
        try {
            HttpResponse response = httpClient.execute(get);  // stops here
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.out.println(response.getStatusLine().getReasonPhrase());
                return Optional.empty();
            }
            JsonElement element = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent()));
            return Optional.of(element.getAsJsonObject());
        } catch (IOException error) {
            error.printStackTrace();
            return Optional.empty();
        } finally {
            get.releaseConnection();
        }
    }
}
