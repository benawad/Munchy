package com.benawad.dunch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class RestaurantParser {
    public static List<Restaurant> getRestaurants(String html) {
        Document doc = Jsoup.parse(html);
        Elements anchors = doc.select("a.biz-name");
        Elements priceCatDivs = doc.select("div.price-category");;
        List<Restaurant> restaurants = new ArrayList<>();
        String name, url, costCat;
        for (int i = 0; i < anchors.size(); i++) {
            name = anchors.get(i).text();
            url = anchors.get(i).attr("href");
            costCat = priceCatDivs.get(i).text();
            if (name != null && !name.equals("") && url != null && !url.equals("")) {
                Restaurant r = new Restaurant(name, url);
                r.setCostCat(costCat);
                restaurants.add(r);
            }
        }
        return restaurants;
    }

    private static String original(String url) {
        int i;
        for (i = url.length() - 1; i > 0; i--) {
            if (url.charAt(i) == '/') {
               break;
            }
        }
        return url.substring(0, i+1) + "o.jpg";
    }

    public static List<String> getPictures(String html) {
        Document doc = Jsoup.parse(html);
        Elements anchors = doc.select("div.photo-box > img");
        List<String> pictures = new ArrayList<>();
        String src;
        for (Element e : anchors) {
            src = e.attr("src");
            if (src != null && !src.equals("")) {
                pictures.add(original(e.attr("src")));
            }
        }
        return pictures;
    }

}
