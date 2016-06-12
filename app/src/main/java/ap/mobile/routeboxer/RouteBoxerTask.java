package ap.mobile.routeboxer;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.Box;
import ap.mobile.routeboxerlib.IRouteBoxer;
import ap.mobile.routeboxerlib.RouteBoxer;

/**
 * Created by aryo on 30/1/16.
 */
public class RouteBoxerTask extends AsyncTask<Void, ArrayList<Box>, ArrayList<Box>> implements IRouteBoxer {

    private final ArrayList<LatLng> route;
    private final int distance;
    private final IRouteBoxerTask iRouteBoxerTask;

    public RouteBoxerTask(ArrayList<LatLng> route, int distance, IRouteBoxerTask iRouteBoxerTask) {
        this.route = route;
        this.distance = distance;
        this.iRouteBoxerTask = iRouteBoxerTask;
    }

    @Override
    protected ArrayList<Box> doInBackground(Void... params) {
        RouteBoxer routeBoxer = new RouteBoxer(route, distance);
        routeBoxer.setRouteBoxerInterface(this);
        return routeBoxer.box();
    }

    @Override
    protected void onPostExecute(ArrayList<Box> boxes) {
        if(this.iRouteBoxerTask != null)
            this.iRouteBoxerTask.onRouteBoxerTaskComplete(boxes);
    }

    @Override
    public void onBoxesObtained(ArrayList<Box> boxes) {}

    @Override
    public void onBoundsObtained(LatLngBounds bounds) {}

    @Override
    public void onGridObtained(Box[][] boxArray) {}

    @Override
    public void onGridMarked(ArrayList<Box> boxes) {}

    @Override
    public void onGridMarksExpanded(Box[][] boxArray) {}

    @Override
    public void onMergedVertically(ArrayList<Box> mergedBoxes) {}

    @Override
    public void onMergedHorizontally(ArrayList<Box> mergedBoxes) {}

    @Override
    protected void onProgressUpdate(ArrayList<Box>... values) {
        if(this.iRouteBoxerTask != null)
            this.iRouteBoxerTask.onRouteBoxerBoxPublished(values[0]);
    }
}
