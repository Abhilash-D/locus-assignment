package com.animall.locusassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "Maps Activity";
    private static int AUTOCOMPLETE_REQUEST_CODE_ORIGIN = 1;
    private static int AUTOCOMPLETE_REQUEST_CODE_DESTINATOIN = 2;

    private GoogleMap mMap;
    CardView searchCard;
    private TextView tvOrigin, tvDestination;
    //private AutocompleteSupportFragment autocompleteOrigin, autocompleteDestination;
    private ImageButton btnSearch;

    private LatLng mOrigin;
    private LatLng mDestination;
    private static final int overview = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();
        initView();
        setListeners();
        initMap();
    }

    private void init(){
        /*mOrigin = new com.google.maps.model.LatLng(12.94523,77.61896);
        mDestination = new com.google.maps.model.LatLng(12.95944, 77.66085);*/

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);
    }

    private void initView(){
        tvOrigin = findViewById(R.id.tv_origin);
        tvDestination = findViewById(R.id.tv_destination);
        /*autocompleteOrigin = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_origin);
        autocompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_destination);
        autocompleteOrigin.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));*/

        btnSearch = findViewById(R.id.btn_search);
        searchCard = findViewById(R.id.search_card);

        searchCard.setVisibility(View.GONE);
    }

    private void initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGIN) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                mOrigin = place.getLatLng();
                tvOrigin.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                mOrigin = null;
            }
            return;
        }else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATOIN) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                mDestination = place.getLatLng();
                tvDestination.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                mDestination = null;
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        searchCard.setVisibility(View.VISIBLE);
    }

    private void setListeners(){
        // Set up a PlaceSelectionListener to handle the response.
        /*autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getLatLng();
            }


            @Override
            public void onError(@NonNull Status status) {
                mOrigin = null;
            }
        });

        autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getLatLng();
            }


            @Override
            public void onError(@NonNull Status status) {
                mDestination = null;
            }
        });*/

        tvOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(MapsActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGIN);
            }
        });

        tvDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(MapsActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINATOIN);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMap != null){
                    if(mOrigin != null && mDestination != null){
                        performSearch(mMap);
                    }else{
                        Toast.makeText(MapsActivity.this, "Please enter both origin and destination", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Log.e(TAG, "Map error : NULL");
                }
            }
        });
    }



    private void performSearch(GoogleMap googleMap){
        DirectionsResult results = getDirectionsDetails(TravelMode.DRIVING);
        if (results != null) {
            Polyline polyline = addPolyline(results, googleMap);
            positionCamera(results.routes[overview], googleMap);
            addMarkersToMap(results, googleMap);

            List<LatLng> pathPoints = getPointsOnPathByDistance(polyline.getPoints(), 200);
            System.out.println("START");
            for(int i = 0; i < pathPoints.size(); i++) {
                LatLng pathPoint = pathPoints.get(i);
                mMap.addMarker(new MarkerOptions().position(pathPoint).title(String.valueOf(i)));
                System.out.println("Path point " + i+1 + " : " + pathPoint.latitude + ", " + pathPoint.longitude);
            }
            System.out.println("END");
        }
    }


    private DirectionsResult getDirectionsDetails(TravelMode mode) {
        DateTime now = new DateTime();
        try {
            com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(mOrigin.latitude, mOrigin.longitude);
            com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(mDestination.latitude, mDestination.longitude);
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .units(Unit.METRIC)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].startLocation.lat,results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].endLocation.lat,results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 15));
    }

    private Polyline addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        return mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private List<LatLng> getPointsOnPathByDistance(List<LatLng> points, final int metres){
        double next = metres;
        double dist = 0;
        double olddist = 0;
        List<LatLng> returnPoints = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            olddist = dist;
            dist += SphericalUtil.computeDistanceBetween(points.get(i), points.get(i-1));
            while (dist > next ) {
                LatLng p1 = points.get(i - 1);
                LatLng p2 = points.get(i);
                //scale factor = dist from 200 / exceeded dist
                double m = (next - olddist) / (dist - olddist);
                //update lat and lng by scale
                LatLng ptToAdd = new LatLng(p1.latitude + (p2.latitude - p1.latitude) * m, p1.longitude + (p2.longitude - p1.longitude) * m);
                returnPoints.add(ptToAdd);

                //update next checkpoint distance
                next += metres;
            }
        }

        return returnPoints;
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }
}