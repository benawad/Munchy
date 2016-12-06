package com.benawad.dunch;


import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindPictures extends AsyncTask<String, String, String> {

    private String baseUrl;
    private OkHttpClient client;

    @Override
    protected String doInBackground(String... strings) {
        String query = "Food";
        String location = "Richardson%2C+TX";
        baseUrl = makeBaseUrl(query, location);
        client = new OkHttpClient();
        return null;
    }

    public List<Restaurant> fetchRestaurants() throws IOException {
        return fetchRestaurants("0");
    }

    public List<Restaurant> fetchRestaurants(String page) throws IOException {
        String url = baseUrl + "&start=" + page;
        return RestaurantParser.getRestaurants(run(url));
    }

    public List<String> fetchPictures(Restaurant restaurant) throws IOException {
        return fetchPictures(restaurant, "0");
    }

    public List<String> fetchPictures(Restaurant restaurant, String page) throws IOException {
        String url = restaurant.getPicUrl() + "?start=" + page;
        return RestaurantParser.getPictures(run(url));
    }

    private String makeBaseUrl(String query, String location) {
       return  "https://www.yelp.com/search?find_desc="+query+"&find_loc="+location+"&ns=1";
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
