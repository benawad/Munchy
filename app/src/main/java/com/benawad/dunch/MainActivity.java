package com.benawad.dunch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Category;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImageView mMainImage;
    TextView mTitle;
    TextView mCostCat;
    ProgressBar mLoading;
    private OkHttpClient mClient;
    final private String iKey = "I_KEY";
    final private String iLastKey = "ILAST_KEY";
    final private String restaurantsKey = "RESTAURANTS_KEY";
    private int i;
    private int iLast;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    boolean waiting = false;
    final private int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 103;
    YelpAPIFactory mApiFactory;
    YelpAPI mYelpApi;
    double mLongitude, mLatitude;
    CoordinateOptions mCoordinate;
    Map<String, String> mParams;
    boolean newSession = false;
    int pageNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainImage = (ImageView) findViewById(R.id.mainImage);
        mTitle = (TextView) findViewById(R.id.restaurantLabel);
        mCostCat = (TextView) findViewById(R.id.costCatLabel);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        mClient = new OkHttpClient();

        mParams = new HashMap<>();
        mParams.put("term", "food");

        if (savedInstanceState != null) {
            i = savedInstanceState.getInt(iKey);
            iLast = savedInstanceState.getInt(iLastKey);
            mRestaurants = savedInstanceState.getParcelableArrayList(restaurantsKey);
        } else {
            i = 0;
            iLast = i;
            newSession = true;
        }

        mMainImage.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                newRestaurant();
            }

            public void onSwipeRight() {
                oldRestaurant();
            }

            @Override
            public void onSwipeTop() {
                sameRestaurantNewPic();
            }

            @Override
            public void onSwipeBottom() {
                sameRestaurantPrevPic();
            }

            @Override
            public void onClick() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mRestaurants.get(i).getMainUrl()));
                startActivity(intent);
            }
        });
        
        mApiFactory = new YelpAPIFactory(
                getString(R.string.consumerKey),
                getString(R.string.consumerSecret),
                getString(R.string.token),
                getString(R.string.tokenSecret));
        mYelpApi = mApiFactory.createAPI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        } else {
            initLocation();
            if (newSession) {
                new FindPictures().execute("0");
            }
            waitForRestaurant(true);
        }

    }

    @SuppressWarnings("MissingPermission")
    public void initLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();
        mCoordinate = CoordinateOptions.builder()
                .latitude(mLatitude)
                .longitude(mLongitude).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initLocation();
                    if (newSession) {
                        new FindPictures().execute("0");
                    }
                    waitForRestaurant(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Location required to get nearby restaurants", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    synchronized public void waitForRestaurant(boolean client) {
        if (client) {
            if (mRestaurants.size() > i && mRestaurants.get(i).getPictures().size() > mRestaurants.get(i).getCurrPic()) {
                mLoading.setVisibility(View.INVISIBLE);
                recipeCallback();
            } else {
                loadingScreen();
                waiting = true;
            }
        } else {
            if (waiting) {
                mLoading.setVisibility(View.INVISIBLE);
                waiting = false;
                recipeCallback();
            }
        }
    }

    public void recipeCallback() {
        displayRestaurant(mRestaurants.get(i));
    }

    public void loadingScreen() {
        mMainImage.setImageResource(android.R.color.transparent);
        mCostCat.setText("");
        mTitle.setText("");
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    private void newRestaurant() {
        if (mRestaurants.size() > i) {
            i++;
            waitForRestaurant(true);
            if (i - iLast > 5 && mRestaurants.size() - i < 10) {
                iLast = i;
                new FindPictures().execute(""+pageNum);
                pageNum += 40;
            }
        }
    }

    private void oldRestaurant() {
        if (i > 0) {
            i--;
            waitForRestaurant(true);
        }
    }

    private void sameRestaurantNewPic() {
        Restaurant currRestaurant = getRestaurant(i);
        if (currRestaurant.getPictures().size() > currRestaurant.getCurrPic()) {
            currRestaurant.incCurrPic();
            waitForRestaurant(true);
            if (currRestaurant.getCurrPic() - currRestaurant.getiLast() > 5 &&
                    currRestaurant.getPictures().size() - currRestaurant.getCurrPic() < 7) {
                currRestaurant.setiLast(currRestaurant.getCurrPic());
                new MorePictures().execute(i);
            }
        }
    }

    private void sameRestaurantPrevPic() {
        Restaurant currRestaurant = getRestaurant(i);
        if (currRestaurant.getCurrPic() > 0) {
            currRestaurant.decCurrPic();
            displayRestaurant(currRestaurant);
        }
    }

    public void displayRestaurant(Restaurant r) {
        Picasso
                .with(MainActivity.this)
                .load(r.getPictures().get(r.getCurrPic()))
                .into(mMainImage);
        mTitle.setText(r.getName());
        mCostCat.setText(r.getCostCat());
    }

    public Restaurant getRestaurant(int pos) {
        if (mRestaurants.size() > pos) {
            return mRestaurants.get(pos);
        } else {
            return null;
        }
    }

    class FindPictures extends AsyncTask<String, Restaurant, String> {

        List<Restaurant> restaurants = null;

        @Override
        protected void onProgressUpdate(Restaurant... values) {
            super.onProgressUpdate(values);
            mRestaurants.add(values[0]);
            waitForRestaurant(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String page = strings[0];
            mParams.put("offset", page);
            retrofit2.Call<SearchResponse> call = mYelpApi.search(mCoordinate, mParams);
            retrofit2.Response<SearchResponse> sr = null;
            try {
                sr = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (sr != null) {
                restaurants = new ArrayList<>();
                List<Business> businesses = sr.body().businesses();
                Collections.shuffle(businesses, new Random(System.nanoTime()));
                for (Business b : businesses) {
                    Restaurant r = new Restaurant(b.name(), b.url());
                    r.setCostCat(b.rating() + " " + categoriesToString(b.categories()));
                    restaurants.add(r);
                    fetchPictures(r, "0", restaurants.size()-1);
                }
            }

            return null;
        }

        private String categoriesToString(List<Category> cats) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cats.size(); i++) {
                sb.append(cats.get(i).name());
                if(i != cats.size()-1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        private void fetchPictures(Restaurant restaurant, String page, final int pos) {
            String url = restaurant.getPicUrl() + "&start=" + page;
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<String> pictures = RestaurantParser.getPictures(response.body().string());
                    if (pictures.size() > 0) {
                        Restaurant r = restaurants.get(pos);
                        r.setPictures(pictures);
                        publishProgress(r);
                    }
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(iKey, i);
        outState.putInt(iLastKey, iLast);
        outState.putParcelableArrayList(restaurantsKey, (ArrayList<? extends Parcelable>) mRestaurants);
        super.onSaveInstanceState(outState);
    }

    class MorePictures extends AsyncTask<Integer, List<String>, String> {

        private int pos;

        @Override
        protected void onProgressUpdate(List<String>... values) {
            super.onProgressUpdate(values);
            mRestaurants.get(pos).getPictures().addAll(values[0]);
            waitForRestaurant(false);
        }

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mClient.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            pos = integers[0];
            Restaurant r = mRestaurants.get(pos);
            String url = r.getPicUrl() + "&start=" + r.getPage();
            r.setPage(r.getPage() + 30);
            String body = null;
            try {
                body = run(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (body != null) {
                List<String> pictures = RestaurantParser.getPictures(body);
                publishProgress(pictures);
            }
            return null;
        }
    }

}
