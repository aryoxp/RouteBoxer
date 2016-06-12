package ap.mobile.routeboxer;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ap.mobile.restclient.Rest;

/**
 * Created by aryo on 28/1/16.
 */
public class RouteTask extends AsyncTask<Void, Void, ArrayList<LatLng>> {

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
        //String json = RoutingHelper.getDummyJson();
        //String json = RoutingHelper.getDummyJSONMalangBlitar();
        ArrayList<LatLng> routes = RoutingHelper.parse(json);
        return routes;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> routes) {
        if (this.IMaps != null)
            this.IMaps.onJSONRouteLoaded(routes);
    }
}
