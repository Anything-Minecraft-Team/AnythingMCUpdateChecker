package org.anythingmc.updatechecker;

public class OldInfo {

    String versions;
    String rating;
    double price;
    String currency;
    String url;
    String status;

    public OldInfo(String versions, String rating, double price, String currency, String url, String status){
        this.versions = versions;
        this.rating = rating;
        this.price = price;
        this.currency = currency;
        this.url = url;
        this.status = status;
    }
}