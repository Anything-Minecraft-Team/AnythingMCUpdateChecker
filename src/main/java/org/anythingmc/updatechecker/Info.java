package org.anythingmc.updatechecker;

import org.anythingmc.updatechecker.enums.Status;

public class Info {

    String versions;
    String rating;
    double price;
    String currency;
    String url;
    Status status;

    public Info(String versions, String rating, double price, String currency, String url, Status status){
        this.versions = versions;
        this.rating = rating;
        this.price = price;
        this.currency = currency;
        this.url = url;
        this.status = status;
    }
}