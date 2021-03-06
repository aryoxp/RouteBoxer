package ap.mobile.routeboxer;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

import ap.mobile.restclient.Rest;

/**
 * Created by aryo on 28/1/16.
 */
public class RouteTask extends AsyncTask<Void, String, ArrayList<LatLng>> {

    private final IMaps IMaps;
    private final LatLng origin;
    private final LatLng destination;

    public RouteTask(IMaps IMaps, LatLng origin, LatLng destination) {
        this.IMaps = IMaps;
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    protected ArrayList<LatLng> doInBackground(Void... params) {
        String origin = this.origin.latitude+","+this.origin.longitude;
        String destination = this.destination.latitude+","+this.destination.longitude;
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+destination;
        String json = Rest.get(url).getString();

        publishProgress(json);



        //String json = RoutingHelper.getDummyJson();
        //String json = RoutingHelper.getDummyJSONMalangBlitar();
        ArrayList<LatLng> routes = RoutingHelper.parse(json);
        return routes;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(this.IMaps != null)
            this.IMaps.routeJsonObtained(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> routes) {
        if (this.IMaps != null)
            try {
                this.IMaps.onJSONRouteLoaded(routes);
            } catch (IOException e) {

            }
    }

}
