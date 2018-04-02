package builderspace.tripadvisor.controller.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.utils.DirectionsJSONParser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng initial;
    private ArrayList<builderspace.tripadvisor.model.Location> locations = new ArrayList<>();
    private DatabaseReference databaseTripsReference;
    private ValueEventListener listener;
    private Trip myTrip;
    private LocationManager locationManager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Trip currentTrip = (Trip) getIntent().getExtras().getSerializable("CURRENT_TRIP");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        databaseTripsReference = FirebaseDatabase.getInstance().getReference("TRIP").child(currentTrip.getKey());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Location location = getLastKnownLocation();

        initial = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    private void setMap() {
        mMap.clear();

        builderspace.tripadvisor.model.Location initialLocation = new builderspace.tripadvisor.model.Location();
        initialLocation.setName("Current Location");
        initialLocation.setLatitude(initial.latitude);
        initialLocation.setLongitude(initial.longitude);
        locations.add(0, initialLocation);
        locations.add(initialLocation);

        for (int i = 0; i < locations.size() - 1; i++) {
            LatLng src = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
            LatLng dest = new LatLng(locations.get(i + 1).getLatitude(), locations.get(i + 1).getLongitude());
            mMap.addMarker(new MarkerOptions().position(src).title(locations.get(i).getName()));
            String url = getDirectionsUrl(src, dest);
            new DownloadTask().execute(url);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        listener = databaseTripsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locations.clear();
                myTrip = dataSnapshot.getValue(Trip.class);
                if (myTrip.getLocations() != null) {
                    locations.addAll(myTrip.getLocations());
                }
                setMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseTripsReference.removeEventListener(listener);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private class DownloadTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... param) {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            List<List<HashMap<String, String>>> routes = null;

            try {
                URL url = new URL(param[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
                JSONObject jsonObject = new JSONObject(data);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jsonObject);

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            } finally {
                try {
                    iStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlConnection.disconnect();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);

            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}
