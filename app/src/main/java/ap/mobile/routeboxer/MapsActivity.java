package ap.mobile.routeboxer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import ap.mobile.routeboxer.helper.FileHelper;
import ap.mobile.routeboxerlib.RouteBoxer;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, IMaps, IRouteBoxerTask,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        DialogInterface.OnClickListener, TestTask.TestInterface {

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
    private ArrayList<RouteBoxer.Box> boxes;
    private ArrayList<Polygon> boxPolygons;
    private DistanceDialog dialog;
    private float defaultZoom = 13;
    private TestingDialog testDialog;
    private MaterialDialog myTestDialog;
    private MaterialDialog routeBoxProcessDialog;


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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            return;
        }
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
                .width(8);
        for (LatLng point : route)
            polylineOptions.add(point);
        if (this.routePolyline != null)
            this.routePolyline.remove();
        this.routePolyline = this.mMap.addPolyline(polylineOptions);
        if (this.boxPolygons == null)
            this.boxPolygons = new ArrayList<>();
        else {
            for (Polygon polygon : this.boxPolygons) {
                polygon.remove();
            }
        }
    }


    @Override
    public void onRouteBoxerTaskComplete(ArrayList<RouteBoxer.Box> boxes) {
        this.draw(boxes, Color.GRAY, Color.argb(15, 255, 0, 0));
        if(this.routeBoxProcessDialog != null && this.routeBoxProcessDialog.isShowing())
            this.routeBoxProcessDialog.dismiss();
    }

    @Override
    public void onRouteBoxerBoxPublished(ArrayList<RouteBoxer.Box> boxes, int step)
    {
        switch (step) {
            case 1:
            case 2:
            case 3:
                this.draw(boxes, Color.GRAY, Color.TRANSPARENT);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                this.draw(boxes, Color.DKGRAY, Color.TRANSPARENT);
                break;
            default:
                this.draw(boxes, Color.GRAY, Color.argb(128, 255, 0, 0));
                break;
        }
    }

    @Override
    public void onMessage(String message) {
        if(this.routeBoxProcessDialog != null && this.routeBoxProcessDialog.isShowing())
            this.routeBoxProcessDialog.setContent(message);
    }

    private void draw(ArrayList<RouteBoxer.Box> boxes, int color, int fillColor) {

        if(this.boxPolygons == null)
            this.boxPolygons = new ArrayList<>();
        else this.boxPolygons.clear();

        for (RouteBoxer.Box box : boxes) {
            LatLng nw = new LatLng(box.ne.latitude, box.sw.longitude);
            LatLng se = new LatLng(box.sw.latitude, box.ne.longitude);

            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(box.sw, nw, box.ne, se, box.sw)
                    .strokeColor(color)
                    .strokeWidth(5);
            if (box.marked) {
                polygonOptions.strokeColor(Color.DKGRAY)
                        .fillColor(Color.argb(96, 0, 0, 0));
            } else if (box.expandMarked) {
                polygonOptions.strokeColor(Color.DKGRAY)
                        .fillColor(Color.argb(72, 0, 0, 0));
            } else
                polygonOptions.fillColor(fillColor);
            Polygon boxPolygon = mMap.addPolygon(polygonOptions);
            this.boxPolygons.add(boxPolygon);
        }

        return;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        this.origin = new LatLng(location.getLatitude(), location.getLongitude());
        //origin = new LatLng(38.595900, -89.985198);
        //origin = new LatLng(38.506380, -89.968063);

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

        this.destination = destination;
        //this.destination = new LatLng(-7.953037137608645,112.63877917081118);
        //this.origin = new LatLng(-7.9545055,112.6148412);

        //this.destination = new LatLng(-7.982594952681266,112.63102859258652);
        //this.origin = new LatLng(-7.9520931,112.6126944);

        MarkerOptions destinationMarkerOptions = new MarkerOptions()
                .title("Destination")
                .position(this.destination)
                .snippet("Click to route box from your location.");



        if(this.destinationMarker != null)
            this.destinationMarker.remove();
        this.destinationMarker = this.mMap.addMarker(destinationMarkerOptions);
        this.destinationMarker.showInfoWindow();


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(this.origin != null && this.destination != null) {
            //origin = new LatLng(38.595900, -89.985198);
            //destination = new LatLng(38.506360, -89.984318);
            //destination = new LatLng(38.506380, -89.968063);
            //origin = new LatLng(38.504700, -89.851810);

            //this.origin = new LatLng(-7.9544773,112.6148372);
            //this.destination = new LatLng(-7.953271897865304,112.63915132731199);

            RouteTask routeTask = new RouteTask(this, this.origin, this.destination);
            if (routeTask.getStatus() == AsyncTask.Status.PENDING)
                routeTask.execute();

            this.routeBoxProcessDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .content("Obtaining boxes...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();

        }
    }

    int bpNum = 3;
    android.os.Handler handler = new Handler();

    Runnable waitRunnable = new Runnable() {
        @Override
        public void run() {
            if(bpNum == 0) {
                if(MapsActivity.this.testDialog != null)
                    MapsActivity.this.testDialog.dismiss();
            }
            else handler.postDelayed(this, 500);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                this.dialog = new DistanceDialog();
                this.dialog.setDistance(this.distance);
                this.dialog.show(this.getSupportFragmentManager(), "distanceDialog");

                return true;

            case R.id.action_test:



                this.testDialog = new TestingDialog();
                this.testDialog.show(this.getSupportFragmentManager(), "testingDialog");

                this.testDialog.text("Getting path...");
                handler.postDelayed(waitRunnable, 1);
                bpNum = 0;


                IMaps vIMaps = new IMaps() {
                    @Override
                    public void onJSONRouteLoaded(ArrayList<LatLng> route) throws IOException {
                        StringBuilder sb = new StringBuilder();
                        for(LatLng pos: route)
                            sb.append(pos.latitude + ";" + pos.longitude + "\n");
                        FileHelper.write(MapsActivity.this, "path-v.txt", sb.toString(), true);
                        bpNum--;
                    }
                };

                IMaps hIMaps = new IMaps() {
                    @Override
                    public void onJSONRouteLoaded(ArrayList<LatLng> route) throws IOException {
                        StringBuilder sb = new StringBuilder();
                        for(LatLng pos: route)
                            sb.append(pos.latitude + ";" + pos.longitude + "\n");
                        FileHelper.write(MapsActivity.this, "path-h.txt", sb.toString(), true);
                        bpNum--;
                    }


                };

                IMaps dIMaps = new IMaps() {
                    @Override
                    public void onJSONRouteLoaded(ArrayList<LatLng> route) throws IOException {
                        StringBuilder sb = new StringBuilder();
                        for(LatLng pos: route)
                            sb.append(pos.latitude + ";" + pos.longitude + "\n");
                        FileHelper.write(MapsActivity.this, "path-d.txt", sb.toString(), true);
                        bpNum--;
                    }
                };
                try {

                    boolean reroute = false;
                    if(bpNum > 0) reroute = true;

                    if (!FileHelper.exists(this, "path-v.txt") || reroute) {
                        LatLng vStart = new LatLng(38.595900, -89.985198);
                        LatLng vEnd = new LatLng(38.506360, -89.984318);
                        RouteTask vRouteTask = new RouteTask(vIMaps, vStart, vEnd);
                        vRouteTask.execute();
                    }

                    if (!FileHelper.exists(this, "path-h.txt") || reroute) {
                        LatLng hStart = new LatLng(38.506380, -89.968063);
                        LatLng hEnd = new LatLng(38.504700, -89.851810);
                        RouteTask hRouteTask = new RouteTask(hIMaps, hStart, hEnd);
                        hRouteTask.execute();
                    }

                    if (!FileHelper.exists(this, "path-d.txt") || reroute) {
                        LatLng dStart = new LatLng(38.621889, -90.153827);
                        LatLng dEnd = new LatLng(38.555006, -90.077097);
                        RouteTask dRouteTask = new RouteTask(dIMaps, dStart, dEnd);
                        dRouteTask.execute();
                    }


                    String vRaw = FileHelper.read(this, "path-v.txt").trim();
                    String[] pairs = vRaw.split("\n");
                    ArrayList<LatLng> points = new ArrayList<>();
                    for(String data: pairs) {
                        String[] cols = data.split(";");
                        LatLng point = new LatLng(Double.parseDouble(cols[0]), Double.parseDouble(cols[1]));
                        points.add(point);
                    }

                    while(points.size() > 100)
                        points.remove((new Random()).nextInt((points.size()-2))+1);

                    String hRaw = FileHelper.read(this, "path-h.txt").trim();
                    String[] hPairs = hRaw.split("\n");
                    ArrayList<LatLng> hPoints = new ArrayList<>();
                    for(String data: hPairs) {
                        String[] cols = data.split(";");
                        LatLng point = new LatLng(Double.parseDouble(cols[0]), Double.parseDouble(cols[1]));
                        hPoints.add(point);
                    }

                    while(hPoints.size() > 100)
                        hPoints.remove((new Random()).nextInt((hPoints.size()-2))+1);

                    String dRaw = FileHelper.read(this, "path-d.txt").trim();
                    String[] vPairs = dRaw.split("\n");
                    ArrayList<LatLng> dPoints = new ArrayList<>();
                    for(String data: vPairs) {
                        String[] cols = data.split(";");
                        LatLng point = new LatLng(Double.parseDouble(cols[0]), Double.parseDouble(cols[1]));
                        dPoints.add(point);
                    }

                    while(dPoints.size() > 100)
                        dPoints.remove((new Random()).nextInt((dPoints.size()-2))+1);


                    TestTask testTask = new TestTask(this, points, dPoints, hPoints);
                    testTask.setStatusInterface(this);
                    testTask.execute();


                } catch (Exception ex) {
                    this.testDialog.text(ex.getMessage());
                }
                //this.testDialog.dismiss();


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

    @Override
    public void start() {
        this.myTestDialog = new MaterialDialog.Builder(this).content("Loading...")
                .cancelable(false)
                .show();
    }

    @Override
    public void showStatus(String status) {
        this.myTestDialog.setContent(status);
    }

    @Override
    public void showError(String error) {
        this.myTestDialog.setContent(error);
    }

    @Override
    public void done() {
        this.myTestDialog.dismiss();
    }
}
