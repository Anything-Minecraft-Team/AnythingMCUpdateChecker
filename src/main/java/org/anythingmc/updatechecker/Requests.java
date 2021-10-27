package org.anythingmc.updatechecker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.anythingmc.updatechecker.enums.Status;
import org.anythingmc.updatechecker.enums.StatusManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.anythingmc.updatechecker.UpdateChecker.config;

public class Requests {

    public static final String SPIGOT_API_URL = "https://api.spiget.org/v2/resources/";
    public HttpClient httpClient;

    public Requests() {
        int timeout = config.requestTimeout * 1000;  // converts the seconds to milliseconds
        RequestConfig requestConfig = RequestConfig.custom() // adds timeout to the http client to speed up requests
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public Optional<JsonObject> getSpigotPluginInfo(String url) {
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        int index = url.lastIndexOf("/");
        if (index == -1) {
            System.out.println("An invalid resource has been passed");
            return Optional.empty();
        }

        String resourceCode = url.substring(index);
        resourceCode = resourceCode.replaceAll("/", "");

        HttpGet get = new HttpGet(SPIGOT_API_URL + resourceCode);
        try {
            HttpResponse response = httpClient.execute(get);  // stops here
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {  // Status code 200 --> OK
                System.out.println(response.getStatusLine().getReasonPhrase());
                return Optional.empty();
            }

            JsonElement element = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent()));
            return Optional.of(element.getAsJsonObject());
        } catch (IOException error) {
            System.out.println("Failed with " + SPIGOT_API_URL + resourceCode);
            return Optional.empty();
        } finally {
            get.releaseConnection();
        }
    }

    public List<Info> getMdFile(String url) {
        try {
            URL newUrl = new URL(url);

            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(newUrl.openStream()));

            List<Info> resources = new ArrayList<>();

            String line, text = "";
            while ((line = in.readLine()) != null) text = text + line + "\n";

            final String regex = "^- \\[[^]\\[]+]\\((https?://[^\\s()]+)\\).*\\R(  Version:.*)\\R(  Rating:.*)\\R(  \\S.+)$";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String version = matcher.group(2).replace("Version: ", "").replaceAll(" ", "");
                String rating = matcher.group(3).replace("Rating: ", "").replaceAll(" ", "");
                String resourceUrl = matcher.group(1);
                Status status = Status.ACTIVE;

                switch (matcher.group(4).toLowerCase().replace(" ", "")) {
                    case "currentlyinactive":
                        status = Status.INACTIVE;
                        break;
                    case "discontinued":
                        status = Status.DISCONTINUED;
                        break;
                    case "lost":
                        status = Status.LOST;
                        break;
                    case "private":
                        status = Status.PRIVATE;
                        break;
                    case "found":
                        status = Status.FOUND;
                        break;
                    case "unreleased":
                        status = Status.UNRELEASED;
                        break;
                }

                if (matcher.group(4).toLowerCase().contains("price")) status = Status.PRICE;

                resources.add(new Info(version, rating, 0.00, "USD", resourceUrl, status));
            }
            in.close();

            return resources;
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return null;
    }
}
