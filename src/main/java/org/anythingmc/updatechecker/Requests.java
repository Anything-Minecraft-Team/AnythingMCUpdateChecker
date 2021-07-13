package org.anythingmc.updatechecker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        //System.out.println(url);
        if(url.endsWith("/"))
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
            System.out.println(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {  // Status code 200 --> OK
                System.out.println(response.getStatusLine().getReasonPhrase());
                return Optional.empty();
            }
            JsonElement element = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent()));
            return Optional.of(element.getAsJsonObject());
        } catch (IOException error) {
            error.printStackTrace();
            System.out.println("ERROR WITH " + get);
            return Optional.empty();
        } finally {
            get.releaseConnection();
        }
    }

    public List<String> getMdFile(String url){
        try {

            URL newUrl = new URL(url);

            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(newUrl.openStream()));

            List<String> urls = new ArrayList<>();

            String line;
            while ((line = in.readLine()) != null) {
                if(line.contains("spigotmc.org/wiki"))
                    continue;
                Pattern pattern = Pattern.compile("(?<=\\().+?(?=\\))");
                Matcher matcher = pattern.matcher(line);
                if(matcher.find())
                    urls.add(matcher.group());
            }
            in.close();

            return urls;
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return null;
    }
}
