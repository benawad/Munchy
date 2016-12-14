package com.benawad.dunch;

import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private String mBaseUrl;
    private OkHttpClient mClient;
    final private String iKey = "I_KEY";
    final private String iLastKey = "ILAST_KEY";
    final private String restaurantsKey = "RESTAURANTS_KEY";
    private int i;
    private int iLast;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    boolean waiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainImage = (ImageView) findViewById(R.id.mainImage);
        mTitle = (TextView) findViewById(R.id.restaurantLabel);
        mCostCat = (TextView) findViewById(R.id.costCatLabel);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        mBaseUrl = makeBaseUrl("Food", "Richardson%2C+TX");
        mClient = new OkHttpClient();

        if (savedInstanceState != null) {
            i = savedInstanceState.getInt(iKey);
            iLast = savedInstanceState.getInt(iLastKey);
            mRestaurants = savedInstanceState.getParcelableArrayList(restaurantsKey);
            displayRestaurant(mRestaurants.get(i));
        } else {
            i = 0;
            iLast = i;
            new FindPictures().execute("0");
            // Initial image
            waitForRestaurant(true);
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
        });


    }

    synchronized public void waitForRestaurant(boolean client) {
        if (client) {
            if (mRestaurants.size() > i) {
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
        mMainImage.setImageResource(0);
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
            if (i > iLast && i %  5 == 0) {
                iLast = i;
                new FindPictures().execute(""+(i*10 / 5));
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
            displayRestaurant(currRestaurant);
            if (currRestaurant.getCurrPic() > currRestaurant.getiLast() &&
                    currRestaurant.getCurrPic() %  15 == 0) {
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

    private String makeBaseUrl(String query, String location) {
        return  "https://www.yelp.com/search?find_desc="+query+"&find_loc="+location+"&ns=1";
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
            try {
                restaurants = fetchRestaurants(page);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (restaurants != null) {
                for (int i = 0; i < restaurants.size(); i++) {
                    fetchPictures(restaurants.get(i), page, i);
                }
            }
            return null;
        }

        private List<Restaurant> fetchRestaurants(String page) throws IOException {
            String url = mBaseUrl + "&start=" + page;
            return RestaurantParser.getRestaurants(run(url));
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

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mClient.newCall(request).execute();
            return response.body().string();
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
