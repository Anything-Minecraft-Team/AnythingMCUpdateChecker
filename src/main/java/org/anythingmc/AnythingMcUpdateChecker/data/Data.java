package org.anythingmc.AnythingMcUpdateChecker.data;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

public class Data {

    public List<String> getLinks(int type){
        /*
         * 0 is spigot
         * 1 is github
         */

        //JSON parser object to parse read file
        JsonParser jsonParser = new JsonParser();

        List<String> list = new ArrayList<>();

        InputStream inputStream = this.getClass().getResourceAsStream("/links.json");
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        //Read JSON file
        Object obj = jsonParser.parse(reader);

        JsonArray jsonArray = (JsonArray) obj;

        //employeeList.forEach(emp -> list.add( emp.getAsJsonObject().toString() ));

        // get the first section in the json list
        JsonObject jsonObject = (JsonObject) jsonArray.get(type);

        // adds all the links from the list
        jsonObject.get("links").getAsJsonArray().forEach(link -> list.add(link.toString()));

        return list;
    }

    public JsonElement getData(String pluginId){
        String USER_AGENT  = "Mozilla/5.0";// Change this!
        String REQUEST_URL = "https://api.spiget.org/v2/resources/" + pluginId.replace("\"", "");

        try {
            URL url = new URL(REQUEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT);// Set User-Agent

            // If you're not sure if the request will be successful,
            // you need to check the response code and use #getErrorStream if it returned an error code
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            // This could be either a JsonArray or JsonObject
            JsonElement element = new JsonParser().parse(reader);
            if (element.isJsonArray()) {
                //System.out.println(element.getAsJsonObject().get("name").getAsString());
            } else if (element.isJsonObject()) {
                // Is JsonObject
                //System.out.println(element.getAsJsonObject().get("name").getAsString());
            } else {
                System.out.println("wtf this shouldn't happen");
            }

            return element;

            // TODO: process element
            //System.out.println(element);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    public String getPluginName(JsonElement json){
        return json.getAsJsonObject().get("name").getAsString();
    }

    public List<String> getPluginVersions(JsonElement json){
        Type listType = new TypeToken<List<String>>() {}.getType();

        return new Gson().fromJson(json.getAsJsonObject().get("testedVersions"), listType);
    }

    public Boolean isSkript(JsonElement json){
        return (json.getAsJsonObject().get("file").getAsJsonObject().get("type").getAsString().equals(".sk"));
    }

    public Boolean isPremium(JsonElement json){
        return (json.getAsJsonObject().get("premium").getAsBoolean());
    }

    public double getPrice(JsonElement json){
        return json.getAsJsonObject().get("price").getAsDouble();
    }

    public int getVersion(JsonElement json){
        return json.getAsJsonObject().get("version").getAsJsonObject().get("id").getAsInt();
    }

    public String getWebhook(){

        //JSON parser object to parse read file
        JsonParser jsonParser = new JsonParser();

        InputStream inputStream = this.getClass().getResourceAsStream("/config.json");
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        //Read JSON file
        Object obj = jsonParser.parse(reader);

        JsonObject jsonArray = (JsonObject) obj;

        // get the first section in the json list

        return jsonArray.getAsJsonObject().get("webhook").getAsString();
    }
}