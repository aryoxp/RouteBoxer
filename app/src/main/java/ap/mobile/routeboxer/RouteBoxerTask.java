package ap.mobile.routeboxer;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.Box;
import ap.mobile.routeboxerlib.RouteBoxer;

/**
 * Created by aryo on 30/1/16.
 */
public class RouteBoxerTask extends AsyncTask<Void, RouteBoxerTask.RouterBoxerData, ArrayList<Box>>
        implements RouteBoxer.IRouteBoxer {

    private final ArrayList<LatLng> route;
    private final int distance;
    private final IRouteBoxerTask iRouteBoxerTask;
    private int step;

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
    public void onGridObtained(Box[][] boxArray, ArrayList<Box> boxes) {
        this.step = 1;
        publishProgress(new RouterBoxerData(null, boxes));
    }

    @Override
    public void onGridMarked(ArrayList<Box> boxes) {
        this.step = 2;
        RouterBoxerData data = new RouterBoxerData(null, boxes);
        publishProgress(data);
    }

    @Override
    public void onGridMarksExpanded(Box[][] boxArray, ArrayList<Box> boxes) {
        this.step = 3;
        RouterBoxerData data = new RouterBoxerData(null, boxes);
        publishProgress(data);
    }

    @Override
    public void onMergedAdjointVertically(ArrayList<Box> boxes) {
        step = 7; // or 5
        RouterBoxerData data = new RouterBoxerData(null, boxes);
        publishProgress(data);
    }

    @Override
    public void onMergedAdjointHorizontally(ArrayList<Box> boxes) {
        step = 5; // or 7
        RouterBoxerData data = new RouterBoxerData(null, boxes);
        publishProgress(data);
    }

    @Override
    public void onMergedVertically(ArrayList<Box> mergedBoxes) {

        step = 6;
        RouterBoxerData data = new RouterBoxerData(null, mergedBoxes);
        publishProgress(data);

    }

    @Override
    public void onMergedHorizontally(ArrayList<Box> mergedBoxes) {

        step = 8;
        RouterBoxerData data = new RouterBoxerData(null, mergedBoxes);
        publishProgress(data);

    }

    @Override
    public void onProcess(String processInfo, ArrayList<Box> boxes) {
        RouterBoxerData data = new RouterBoxerData(processInfo, boxes);
        publishProgress(data);
    }

    @Override
    protected void onProgressUpdate(RouterBoxerData... values) {
        if(this.iRouteBoxerTask != null) {
            RouterBoxerData data = values[0];
            String message = data.message;
            ArrayList<Box> boxes = data.boxes;
            //if(boxes != null)
                //this.iRouteBoxerTask.onRouteBoxerBoxPublished(boxes, this.step);
            if(message != null)
                this.iRouteBoxerTask.onMessage(message);
            switch(this.step) {
                case 1:
                    break;
            }
        }
    }

    public class RouterBoxerData {

        public ArrayList<Box> boxes;
        public String message;

        public RouterBoxerData(String message, ArrayList<Box> boxes) {
            this.message = message;
            this.boxes = boxes;
        }

    }
}
