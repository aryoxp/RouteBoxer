package ap.mobile.routeboxer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.Box;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, IMaps, IRouteBoxerTask,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        DialogInterface.OnClickListener {

    private Toolbar myToolbar;
    private GoogleMap mMap;

    private int distance = 200; // meter

    private LatLngBounds bounds;
    private Polyline routePolyline;
    private GoogleApiClient mGoogleApiClient;
    private Marker originMarker;
    private Marker destinationMarker;
    private LatLng destination;
    private LatLng origin;
    private ArrayList<Box> boxes;
    private ArrayList<Polygon> boxPolygons;
    private DistanceDialog dialog;
    private float defaultZoom = 13;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.activity_maps);
        this.getSupportActionBar().setHomeButtonEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    protected void onStart() {
        this.mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        this.mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap = googleMap;
        this.mMap.setMyLocationEnabled(true);
        this.mMap.setOnMapLongClickListener(this);
        this.mMap.setOnInfoWindowClickListener(this);

        UiSettings uiSettings = this.mMap.getUiSettings();

        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        if (this.origin != null)
            this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.origin, this.defaultZoom));

        /*

        LatLng malang = new LatLng(-7.9637216, 112.6267371);
        LatLng surabaya = new LatLng(-7.2627018, 112.7533774);
        LatLng blitar = new LatLng(-8.0948239, 112.1301506);

        this.bounds = LatLngBounds.builder().include(malang).include(blitar).build();

        this.markerMalang = this.mMap.addMarker(new MarkerOptions().position(malang).title("Malang"));
        this.markerSurabaya = this.mMap.addMarker(new MarkerOptions().position(surabaya).title("Surabaya"));
        this.markerBlitar = this.mMap.addMarker(new MarkerOptions().position(blitar).title("Blitar"));

        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));

        RouteTask routeTask = new RouteTask(this, malang, blitar);
        if (routeTask.getStatus() == AsyncTask.Status.PENDING)
            routeTask.execute();
        */


    }

    @Override
    public void onJSONRouteLoaded(ArrayList<LatLng> route) {
        RouteBoxerTask routeBoxerTask = new RouteBoxerTask(route, this.distance, this);
        routeBoxerTask.execute();
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(5);
        for(LatLng point: route)
            polylineOptions.add(point);
        if(this.routePolyline != null)
            this.routePolyline.remove();
        this.routePolyline = this.mMap.addPolyline(polylineOptions);
        if(this.boxPolygons == null)
            this.boxPolygons = new ArrayList<>();
        else {
            for(Polygon polygon: this.boxPolygons) {
                polygon.remove();
            }
        }
    }


    @Override
    public void onRouteBoxerTaskComplete(ArrayList<Box> boxes) {
        this.draw(boxes, Color.GRAY, Color.argb(15, 255, 0, 0));
    }

    @Override
    public void onRouteBoxerBoxPublished(ArrayList<Box> boxes) {
        this.draw(boxes, Color.GRAY, Color.argb(128, 255, 0, 0));
    }

    private void draw(ArrayList<Box> boxes, int color, int fillColor) {
        for(Box box: boxes) {
            LatLng nw = new LatLng(box.ne.latitude, box.sw.longitude);
            LatLng se = new LatLng(box.sw.latitude, box.ne.longitude);

            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(box.sw, nw, box.ne, se, box.sw)
                    .strokeColor(color)
                    .strokeWidth(3);
            if (box.marked) {
                polygonOptions.strokeColor(Color.RED)
                        .fillColor(Color.YELLOW);
            } else if (box.expandMarked) {
                polygonOptions.strokeColor(Color.BLUE)
                        .fillColor(Color.GREEN);
            } else
                polygonOptions.fillColor(fillColor);
            Polygon boxPolygon = mMap.addPolygon(polygonOptions);
            this.boxPolygons.add(boxPolygon);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        this.origin = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.origin == null)
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Unable to obtain your last known location. Please enable Location on Settings.")
                    .show();
        if (this.mMap != null && this.origin != null) {
            this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.origin, this.defaultZoom));
            MarkerOptions originMarkerOptions = new MarkerOptions()
                    .title("Your location")
                    .snippet("Your last known location")
                    .position(this.origin)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            if (this.originMarker != null)
                this.originMarker.remove();
            this.originMarker = this.mMap.addMarker(originMarkerOptions);
        }
        this.mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng destination) {

        MarkerOptions destinationMarkerOptions = new MarkerOptions()
                .title("Destination")
                .position(destination)
                .snippet("Click to route box from your location.");

        if(this.destinationMarker != null)
            this.destinationMarker.remove();
        this.destinationMarker = this.mMap.addMarker(destinationMarkerOptions);
        this.destinationMarker.showInfoWindow();
        this.destination = destination;

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(this.origin != null && this.destination != null) {
            RouteTask routeTask = new RouteTask(this, origin, destination);
            if (routeTask.getStatus() == AsyncTask.Status.PENDING)
                routeTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                this.dialog = new DistanceDialog();
                this.dialog.setDistance(this.distance);
                this.dialog.show(this.getSupportFragmentManager(), "distanceDialog");

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                this.distance = (int) this.dialog.distance;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }
}
