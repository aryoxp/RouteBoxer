package ap.mobile.routeboxerlib;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by aryo on 14/6/16.
 */
public class Route {

    private ArrayList<LatLng> path;

    public Route(ArrayList<LatLng> path) {
        this.path = path;
    }

    public void add(LatLng vertex) {
        if(this.path == null)
            this.path = new ArrayList<>();
        this.path.add(vertex);
    }

    public ArrayList<LatLng> getRoute() {
        return this.path;
    }

    public void clear() {
        if(this.path != null)
            this.path.clear();
    }

}
