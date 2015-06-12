package com.android.yardsale.helpers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.yardsale.activities.ListActivity;
import com.android.yardsale.activities.SearchActivity;
import com.android.yardsale.activities.YardSaleDetailActivity;
import com.android.yardsale.models.Item;
import com.android.yardsale.models.YardSale;
import com.facebook.FacebookSdk;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YardSaleApplication extends Application {
    public static final String YARDSALE_APPLICATION_ID = "MMFnruGWoh34ACUP3e4z7MPpn4zjU7eSTtgv4t6o";
    public static final String YARDSALE_CLIENT_KEY = "1hYdGwa3TljmkHjwJnuBSvUjTlHE7UT9iByTPy7x";
    public static final int MAP_ZOOM = 13;
    private static Context context;
    private Activity callingActivity;

    public YardSaleApplication() {
        super();
    }

    public YardSaleApplication(Activity activity) {
        this.callingActivity = activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getBaseContext();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        ParseObject.registerSubclass(YardSale.class);
        ParseObject.registerSubclass(Item.class);
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, YARDSALE_APPLICATION_ID, YARDSALE_CLIENT_KEY);
        ParseUser.enableAutomaticUser();
        ParseFacebookUtils.initialize(this);



    }

    public void manualSignUp(final String userName, final String password) {
        ParseUser user = new ParseUser();
        user.setUsername(userName);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Signup success
                    Toast.makeText(context, "Signup Success", Toast.LENGTH_LONG).show();
                    login(userName, password);

                } else {
                    Toast.makeText(context, "Signup Failed", Toast.LENGTH_LONG).show();
                    Log.d("DEBUG", "SIGNUP FAILED", e);
                    // signup failed
                }
            }
        });
    }

    public void login(final String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    Toast.makeText(context, "Logged In!!!!!", Toast.LENGTH_LONG).show();
                    getYardSales();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Toast.makeText(context, "Login failed :(", Toast.LENGTH_LONG).show();
                    Log.d("DEBUG", "SIGNUP FAILED", e);
                }
            }
        });
    }

    private void launchListActivity(ArrayList<CharSequence> saleList) {
        Intent intent = new Intent(context, ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putCharSequenceArrayListExtra("sale_list", saleList);
        callingActivity.startActivity(intent);
    }

    public void logout() {
        //FB.logout();
        ParseUser.logOut();
    }

    public void signUpAndLoginWithFacebook(List<String> permissions) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(callingActivity, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                //TODO Progress bars
                if (user == null) {
                    Log.d("DEBUG", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("DEBUG", "User signed up and logged in through Facebook!");
                    //TODO go to the add info to profile page
                    //showUserDetailsActivity();
                    getYardSales();
                } else {
                    Log.d("DEBUG", "User logged in through Facebook!");
                    getYardSales();
                }
            }
        });
    }

    public void createYardSale(String title, String description, Date startTime, Date endTime, String address) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        String toastMessage = "title: " + title + " description: " + description + " startTime: " + dateFormat.format(startTime) + " endTime: " + dateFormat.format(endTime) + " address: " + address;
        Toast.makeText(callingActivity, toastMessage, Toast.LENGTH_LONG).show();
        //TODO get location from the given address
        //LatLng latLong = GeopointUtils.getLocationFromAddress(callingActivity, address);
        //ParseGeoPoint location = new ParseGeoPoint(latLong.latitude, latLong.longitude);

        YardSale yardSale = new YardSale();
        yardSale.setTitle(title);
        yardSale.setDescription(description);
        yardSale.setAddress(address);
        yardSale.setSeller(ParseUser.getCurrentUser());
        yardSale.setStartTime(startTime);
        yardSale.setEndTime(startTime);
        yardSale.saveInBackground();
    }

    public void getYardSales() {
        ParseQuery<YardSale> query = ParseQuery.getQuery(YardSale.class);
        query.include("seller");
        query.findInBackground(new FindCallback<YardSale>() {
            public void done(List<YardSale> itemList, ParseException e) {
                if (e == null) {
                    //String firstItemId = itemList.get(0).getObjectId();
                    ArrayList<CharSequence> saleList = new ArrayList<>();
                    for (YardSale sale : itemList) {
                        saleList.add(sale.getObjectId());
                    }
                    launchListActivity(saleList);

                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }

            }
        });
    }

    public void searchForItems(String query) {
        ParseQuery<Item> searchQuery = ParseQuery.getQuery(Item.class);
        searchQuery.whereMatches("description", "(" + query + ")", "i");
        searchQuery.findInBackground(new FindCallback<Item>() {
            public void done(List<Item> results, ParseException e) {
                if (e == null) {
                    launchSearchActivity(results);
                    //   objectsWereRetrievedSuccessfully(objects);
                } else {
                    //  objectRetrievalFailed();
                }
            }
        });
    }


    public void createItem(String description, Number price, ParseFile photo, YardSale yardSale) {
        Item item = new Item(description, price, photo, yardSale);
        if (yardSale.getCoverPic() == null)
            setPicForYardSale(yardSale, photo);
        item.saveInBackground();
    }

    public void setPicForYardSale(YardSale sale, ParseFile photo) {
        sale.setCoverPic(photo);
        sale.saveInBackground();
    }

    public void getItemsForYardSale(final YardSale yardSale) {
        // Define the class we would like to query
        ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
        query.whereEqualTo("yardsale_id", yardSale);
        query.findInBackground(new FindCallback<Item>() {
            public void done(List<Item> itemList, ParseException e) {
                if (e == null) {
                    launchYardSaleDetailActivity(yardSale, itemList);
                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });
    }

    private static void launchYardSaleDetailActivity(YardSale yardsale, List<Item> items) {
        Intent intent = new Intent(context, YardSaleDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ArrayList<CharSequence> itemObjList = new ArrayList<>();
        for (Item item : items) {
            itemObjList.add(item.getObjectId());
        }
        intent.putCharSequenceArrayListExtra("item_list", itemObjList);
        intent.putExtra("yardsale", yardsale.getObjectId());
        context.startActivity(intent);
    }

    //TODO may be generalize this
    private static void launchSearchActivity(List<Item> items) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle data = new Bundle();
        ArrayList<String> seachItems = new ArrayList<>();
        for (Item item : items) {
            seachItems.add(item.getDescription());
        }
        data.putStringArrayList("search", seachItems);
        intent.putExtras(data);
        context.startActivity(intent);
    }

    private double getLatitude(JSONObject json) {
        return ((JSONArray) json.opt("results")).optJSONObject(0)
                .optJSONObject("geometry").optJSONObject("location")
                .optDouble("lat");
    }

    private double getLongitude(JSONObject json) {
        return ((JSONArray) json.opt("results")).optJSONObject(0)
                .optJSONObject("geometry").optJSONObject("location")
                .optDouble("lng");
    }

    public void deleteSale(YardSale sale) {
        sale.deleteInBackground();

        ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
        query.whereEqualTo("yardsale_id", sale);
        query.findInBackground(new FindCallback<Item>() {
            public void done(List<Item> itemList, ParseException e) {
                if (e == null) {
                    for(Item item:itemList)
                        item.deleteInBackground();
                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });

//        Naturally we can also delete in an offline manner with:
//
//        todoItem.deleteEventually();
    }
}
