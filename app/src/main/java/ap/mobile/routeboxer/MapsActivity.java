package ap.mobile.routeboxer;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ap.mobile.routeboxer.helper.FileHelper;
import ap.mobile.routeboxerlib.RouteBoxer;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, IMaps, RouteBoxerTask.IRouteBoxerTask,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        DialogInterface.OnClickListener, TestTask.TestInterface {

    private static final int NOTIFICATION_ID = 99;
    private static final int WEAR_REQUEST_CODE = 77;
    private static final int WEAR_REQUEST_CODE_2 = 88;

    private Toolbar myToolbar;
    private GoogleMap mMap;

    private int distance = 200; // meter

    private LatLngBounds bounds;
    private Polyline routePolyline, simplifiedRoutePolyline;
    private GoogleApiClient mGoogleApiClient;
    private Marker originMarker;
    private Marker destinationMarker;
    private LatLng destination;
    private LatLng origin;
    private ArrayList<RouteBoxer.Box> boxes;
    private ArrayList<Polygon> boxPolygons;
    private ArrayList<Polygon> gridBoxes;
    private DistanceDialog dialog;
    private float defaultZoom = 13;
    private TestingDialog testDialog;
    private MaterialDialog myTestDialog;
    private MaterialDialog routeBoxProcessDialog;
    private String json;
    private WearActionReceiver wearActionReceiver;
    private Context mContext;


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

        this.mContext = this;

        this.wearActionReceiver = new WearActionReceiver() {

            @Override
            public void onDismiss() {
                Toast.makeText(mContext, "Dismissed from Wear", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecalculate() {
                Toast.makeText(mContext, "Recalculate from Wear", Toast.LENGTH_SHORT).show();
            }
        };

        this.registerReceiver(this.wearActionReceiver, new IntentFilter(WearActionReceiver.WEAR_ACTION));


        //LocalBroadcastManager.getInstance(this).registerReceiver(
        //        this.wearActionReceiver, new IntentFilter("routeboxer.wearintent")
        //);


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

    public static class Route implements Serializable {
        public ArrayList<LatLng> points;
        public Route(ArrayList<LatLng> points){
            this.points = points;
        }
    }

    @Override
    public void onJSONRouteLoaded(ArrayList<LatLng> route) {

        Route r = new Route(route);
        Log.d("RouteBoxer", r.toString());

        boolean simplify = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_simplify", true);
        boolean runBoth = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_runboth", false);

        RouteBoxerTask routeBoxerTask = new RouteBoxerTask(route, this.distance, simplify, runBoth, this);
        routeBoxerTask.execute();

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(8);
        for (LatLng point : route)
            polylineOptions.add(point);
        if (this.routePolyline != null)
            this.routePolyline.remove();
        this.routePolyline = this.mMap.addPolyline(polylineOptions);
        //this.routePolyline.setPattern(Arrays.asList(new Dash(30), new Gap(10)));
        if (this.boxPolygons == null)
            this.boxPolygons = new ArrayList<>();
        else {
            for (Polygon polygon : this.boxPolygons) {
                polygon.remove();
            }
        }

        if(this.gridBoxes != null) {
            for(Polygon polygon: this.gridBoxes)
                polygon.remove();
        }
    }

    @Override
    public void routeJsonObtained(String json) {

        this.json = json;
        if(isStoragePermissionGranted())
            this.writeJsonToFile(json);

    }



    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("RouteBoxer","Permission is granted");
                return true;
            } else {

                Log.v("RouteBoxer","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("RouteBoxer","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            this.writeJsonToFile(this.json);
    }

    private void writeJsonToFile(String json) {
        if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            //saveButton.setEnabled(false);

            String filepath = "RouteBoxer";
            String indexFilename = "idx.txt";
            int index = 0;

            File indexFile = new File(getExternalFilesDir(filepath), indexFilename);
            String myData = "";

            try {
                FileInputStream fis = new FileInputStream(indexFile);
                DataInputStream in = new DataInputStream(fis);
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    myData = myData + strLine;
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(myData == "") index = 0;
            else index = Integer.valueOf(myData);

            try {
                FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir(filepath), "route-" + index + ".txt"));
                fos.write(json.getBytes());
                fos.close();

                index++;

                fos = new FileOutputStream(indexFile);
                fos.write(String.valueOf(index).getBytes());
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRouteBoxerTaskComplete(ArrayList<RouteBoxer.Box> boxes) {
        this.draw(boxes, Color.argb(128, 255, 0, 0), Color.argb(15, 255, 0, 0));
        if(this.routeBoxProcessDialog != null && this.routeBoxProcessDialog.isShowing())
            this.routeBoxProcessDialog.dismiss();
        this.showNotification();
    }

    private void showNotification() {
        Intent dismissIntent = new Intent(WearActionReceiver.WEAR_ACTION);
        dismissIntent.putExtra(WearActionReceiver.WEAR_ACTION_CODE, WearActionReceiver.DISMISS_NOTIFICATION);

        PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(mContext, WEAR_REQUEST_CODE,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent recalculateIntent = new Intent(WearActionReceiver.WEAR_ACTION);
        recalculateIntent.putExtra(WearActionReceiver.WEAR_ACTION_CODE, WearActionReceiver.RECALCULATE);

        PendingIntent pendingIntentRecalculate = PendingIntent.getBroadcast(mContext, WEAR_REQUEST_CODE_2,
                recalculateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_call_split)
                .setContentTitle("RouteBoxer")
                .setContentText("RouteBoxer has completed the computation process")
                .addAction(R.mipmap.ic_launcher_round, "Dismiss", pendingIntentDismiss)
                .addAction(R.mipmap.ic_launcher_round, "Recalculate", pendingIntentRecalculate);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onRouteBoxerMessage(String message) {
        if(this.routeBoxProcessDialog != null && this.routeBoxProcessDialog.isShowing())
            this.routeBoxProcessDialog.setContent(message);
    }

    @Override
    public void onRouteBoxerGrid(ArrayList<RouteBoxer.Box> boxes, int boxBorderColor, int markedColor, int simpleMarkedColor) {
        if(this.gridBoxes == null)
            this.gridBoxes = new ArrayList<>();
        else this.gridBoxes.clear();

        for (RouteBoxer.Box box : boxes) {
            LatLng nw = new LatLng(box.ne.latitude, box.sw.longitude);
            LatLng se = new LatLng(box.sw.latitude, box.ne.longitude);
            LatLng sw = new LatLng(box.sw.latitude, box.sw.longitude);
            LatLng ne = new LatLng(box.ne.latitude, box.ne.longitude);
            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(sw, nw, ne, se, sw)
                    .strokeColor(boxBorderColor)
                    .strokeWidth(3);
            if (box.simpleMarked) {
                polygonOptions.strokeColor(boxBorderColor)
                        .fillColor(simpleMarkedColor);
            } else if (box.marked) {
                polygonOptions.strokeColor(boxBorderColor)
                        .fillColor(markedColor);
            } else
                polygonOptions.fillColor(Color.TRANSPARENT);
            Polygon boxPolygon = mMap.addPolygon(polygonOptions);
            this.gridBoxes.add(boxPolygon);
        }
    }

    @Override
    public void onRouteBoxerBoxes(ArrayList<RouteBoxer.Box> boxes, int boxBorderColor, int boxFillColor) {}

    @Override
    public void onRouteBoxerSimplifiedRoute(ArrayList<LatLng> simplifiedRoute, int lineColor) {
        boolean simplify = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_simplify", true);
        if(!simplify) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(lineColor)
                .width(8);
        for (LatLng point : simplifiedRoute)
            polylineOptions.add(point);
        if (this.simplifiedRoutePolyline != null)
            this.simplifiedRoutePolyline.remove();
        this.simplifiedRoutePolyline = this.mMap.addPolyline(polylineOptions);
        List<PatternItem> pattern = Arrays.asList(
                new Dash(30), new Gap(10));
        this.simplifiedRoutePolyline.setPattern(pattern);
    }

    private void draw(ArrayList<RouteBoxer.Box> boxes, int color, int fillColor) {

        if(this.boxPolygons == null)
            this.boxPolygons = new ArrayList<>();
        else this.boxPolygons.clear();

        for (RouteBoxer.Box box : boxes) {
            LatLng nw = new LatLng(box.ne.latitude, box.sw.longitude);
            LatLng se = new LatLng(box.sw.latitude, box.ne.longitude);
            LatLng sw = new LatLng(box.sw.latitude, box.sw.longitude);
            LatLng ne = new LatLng(box.ne.latitude, box.ne.longitude);
            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(sw, nw, ne, se, sw)
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
                .snippet("Tap to RouteBox");

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
            /*
            this.routeBoxProcessDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .content("Obtaining boxes...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();
            */
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

                /*
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


                    //TestTask testTask = new TestTask(this, points, dPoints, hPoints);
                    //testTask.setStatusInterface(this);
                    //testTask.execute();


                } catch (Exception ex) {
                    this.testDialog.text(ex.getMessage());
                }
                //this.testDialog.dismiss();
                */

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
