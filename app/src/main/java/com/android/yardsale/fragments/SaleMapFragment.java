package com.android.yardsale.fragments;

import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.yardsale.R;
import com.android.yardsale.helpers.CustomMapInfoWindowAdapter;
import com.android.yardsale.helpers.YardSaleApplication;
import com.android.yardsale.models.YardSale;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SaleMapFragment extends SupportMapFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static YardSale yardSale;
    private static List<YardSale> yardSaleList;
    private static BitmapDescriptor defaultMarker ;
    private FloatingActionButton btFlip;
    private Button btDetailView;
    static Context context;
    FrameLayout flMap;
    static List<Marker> markers;
    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private boolean doneAnim=false;

    public SaleMapFragment(){
        super();
    }


    public static SaleMapFragment newInstance(YardSale sale,Context c){

        SaleMapFragment fragmentDemo = new SaleMapFragment();
        yardSale = sale;
        yardSaleList = null;
        //Bundle args = new Bundle();
        //args.putInt("sale_list", list);
        //fragmentDemo.setArguments(args);
        markers = new ArrayList<>();
        return fragmentDemo;
    }

    public static SaleMapFragment newInstance(List<YardSale> saleList,Context c){

        SaleMapFragment fragmentDemo = new SaleMapFragment();
        yardSaleList = saleList;
        yardSale = null;
        //Bundle args = new Bundle();
        //args.putInt("sale_list", list);
        //fragmentDemo.setArguments(args);
        context = c;
        markers = new ArrayList<>();
        return fragmentDemo;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        //defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        defaultMarker = BitmapDescriptorFactory.fromResource(R.drawable.ys1);

       View v;

       if(yardSaleList!=null ) {
           v = inflater.inflate(R.layout.fragment_map, parent, false);
            flMap = (FrameLayout) v.findViewById(R.id.flMap);
            btFlip = (FloatingActionButton) v.findViewById(R.id.fab);

            View map = super.onCreateView(inflater, parent, savedInstanceState);
            flMap.addView(map);

            //should only show on view all
            btFlip.setVisibility(View.VISIBLE);
            btFlip.setImageDrawable((getResources().getDrawable(R.drawable.list_bulleted)));
            btFlip.setColorNormal(R.color.accent_color);
            btFlip.setColorPressed(R.color.accent_color);
            btFlip.setColorNormal(getResources().getColor(R.color.accent_color));
            btFlip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out);

//                    ViewAnimator viewFlipper = new FlipAnimation(getActivity());
//                    AnimationFactory.flipTransition(viewFlipper, AnimationFactory.FlipDirection.RIGHT_LEFT);
                    SalesFragment frag;
                    if (fragmentManager.findFragmentByTag("list_frag") == null) {
                        frag = SalesFragment.newInstance();
                    } else {
                        frag = (SalesFragment) fragmentManager.findFragmentByTag("list_frag");
                    }
                    //frag.setEnterTransition();//R.anim.new Slide(Gravity.RIGHT));
                    //frag.setExitTransition();
//                    transaction.setCustomAnimations(new com.daimajia.androidanimations.library.flippers.FlipInXAnimator(), R.anim.flip_in);

                    transaction.replace(R.id.flContent, frag).addToBackStack("list_frag").commit();
                }
            });


        }else{
             v = super.onCreateView(inflater, parent, savedInstanceState);
       }
        initMap(inflater);
        return v;
    }

    private void initMap(LayoutInflater inflater){
        UiSettings settings = getMap().getUiSettings();

        getMap().setMyLocationEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        final CustomMapInfoWindowAdapter windowAdapter = new CustomMapInfoWindowAdapter(getActivity().getSupportFragmentManager(), inflater, getActivity());
        getMap().setInfoWindowAdapter(windowAdapter);

        if (yardSaleList!=null ) {
            for (YardSale s : yardSaleList) {
                addYardSale(s,true);
            }
            getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    windowAdapter.callDetailActivity();
                }
            });
        } else {
            if(yardSale!=null) {
                addYardSale(yardSale, false);
            }
            settings.setAllGesturesEnabled(false);
            settings.setMyLocationButtonEnabled(false);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            Log.d("Location Updates", "Google Play services is not available.");
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void addYardSale(YardSale row, boolean fromlist){
        if(row.getLocation() == null)
            return;
        LatLng loc = new LatLng(row.getLocation().getLatitude(),row.getLocation().getLongitude());
        //getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12));
        if(fromlist) {
            Marker marker = getMap().addMarker(new MarkerOptions()
                    .position(loc)
                    .title(row.getTitle() + "::::" + row.getObjectId())
                    .icon(defaultMarker).snippet(row.getAddress()));
            markers.add(marker);
        }else{
            Marker marker = getMap().addMarker(new MarkerOptions()
                    .position(loc)
                    .title(row.getTitle())
                    .icon(defaultMarker).snippet(row.getAddress()));
            markers.add(marker);
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //Toast.makeText(getActivity(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            CameraUpdate cameraUpdate;
            //Todo If loc for the selected sale is null then just display current loca?
            if(yardSale == null || yardSale.getTitle()==null || yardSale.getLocation() == null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, YardSaleApplication.MAP_ZOOM);
            } else {
                LatLng latLng = new LatLng(yardSale.getLocation().getLatitude(), yardSale.getLocation().getLongitude());
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, YardSaleApplication.MAP_ZOOM);
            }
            getMap().animateCamera(cameraUpdate);
            startLocationUpdates();
        } else {
            Toast.makeText(getActivity(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Toast.makeText(getActivity(), "Long Press", Toast.LENGTH_LONG).show();
        // Custom code here...
     //   showAlertDialogForPoint(latLng);

    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
//        String msg = "Updated Location: " +
//                Double.toString(location.getLatitude()) + "," +
//                Double.toString(location.getLongitude());
       // Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

    }

    /*
     * Called by Location Services if the connection to the location com.client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(getActivity(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    //called from detail activity only to see marker for 1 sale
    public void addMarker(YardSale ys){
        if(markers!=null) {
            for (Marker m : markers)
                m.remove();
        }else{
            markers = new ArrayList<>();
        }
        if(btFlip!=null)
            btFlip.setVisibility(View.GONE);
        yardSaleList = null;
        yardSale = ys;
        addYardSale(ys,false);
    }

    //for all sales
    public void addSaleToList(YardSale s){
        yardSaleList.add(s);
        addYardSale(s,true);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
//        Point pointSize = new Point();
//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        display.getSize(pointSize);
//        int screenWidth = pointSize.x;
//
//        ObjectAnimator moveFab = ObjectAnimator.ofFloat(btFlip, "translationX", 0, -(screenWidth / 3));
//        moveFab.setInterpolator(new AccelerateInterpolator());
//        moveFab.setDuration(1000);
//        moveFab.start();


//            //button in center
//            LinearLayout lLayout = (LinearLayout) getActivity().findViewById(R.id.llLayout);
//            LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) lLayout.getLayoutParams();
//            ll.gravity = Gravity.CENTER;
//            btFlip.setLayoutParams(ll);

        return true;
    }


}
