package ap.mobile.routeboxer;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.RouteBoxer;

public class RouteBoxerTask extends AsyncTask<Void, RouteBoxerTask.RouterBoxerData, ArrayList<RouteBoxer.Box>>
        implements RouteBoxer.IRouteBoxer {

    private final ArrayList<RouteBoxer.LatLng> route = new ArrayList<>();
    private final int distance;
    private final IRouteBoxerTask iRouteBoxerTask;
    private int step;

    public RouteBoxerTask(ArrayList<LatLng> route, int distance, IRouteBoxerTask iRouteBoxerTask) {
        for (LatLng point:
                route) {
            RouteBoxer.LatLng latLng = new RouteBoxer.LatLng(point.latitude, point.longitude);
            this.route.add(latLng);
        }
        this.distance = distance;
        this.iRouteBoxerTask = iRouteBoxerTask;
    }

    @Override
    protected ArrayList<RouteBoxer.Box> doInBackground(Void... params) {
        RouteBoxer routeBoxer = new RouteBoxer(route, distance);
        routeBoxer.setRouteBoxerInterface(this);
        return routeBoxer.box();
    }

    @Override
    protected void onPostExecute(ArrayList<RouteBoxer.Box> boxes) {
        if(this.iRouteBoxerTask != null)
            this.iRouteBoxerTask.onRouteBoxerTaskComplete(boxes);
    }

    @Override
    public void onBoxesObtained(ArrayList<RouteBoxer.Box> boxes) {}

    @Override
    public void onBoundsObtained(RouteBoxer.LatLngBounds bounds) {}

    @Override
    public void onGridOverlaid(ArrayList<RouteBoxer.Box> boxes) {}

    @Override
    public void onGridObtained(RouteBoxer.Box[][] boxArray) {}

    @Override
    public void onGridMarked(ArrayList<RouteBoxer.Box> boxes) {}

    @Override
    public void onGridMarksExpanded(RouteBoxer.Box[][] boxArray) {}

    @Override
    public void onMergedAdjointVertically(ArrayList<RouteBoxer.Box> boxes) {}

    @Override
    public void onMergedAdjointHorizontally(ArrayList<RouteBoxer.Box> boxes) {}

    @Override
    public void onMergedVertically(ArrayList<RouteBoxer.Box> mergedBoxes) {}

    @Override
    public void onMergedHorizontally(ArrayList<RouteBoxer.Box> mergedBoxes) {}

    @Override
    public void onProcess(String processInfo) {
        RouterBoxerData data = new RouterBoxerData(processInfo);
        publishProgress(data);
    }

    @Override
    public void drawLine(RouteBoxer.LatLng origin, RouteBoxer.LatLng destination, int color) {}

    @Override
    public void drawBox(RouteBoxer.LatLng origin, RouteBoxer.LatLng destination, int color) {}

    @Override
    public void clearPolygon() {}

    @Override
    protected void onProgressUpdate(RouterBoxerData... values) {
        if(this.iRouteBoxerTask != null) {
            RouterBoxerData data = values[0];

            if (data.type == DataType.Message) {
                if (data.message != null)
                    this.iRouteBoxerTask.onRouteBoxerMessage(data.message);
            } else if(data.type == DataType.Boxes){
                if(data.boxes != null)
                    this.iRouteBoxerTask.onRouteBoxerBoxes(data.boxes, data.boxBorderColor, data.boxFillColor);
            }

        }
    }

    public class RouterBoxerData {


        private DataType type = DataType.Message;
        private ArrayList<RouteBoxer.Box> boxes;
        private String message;
        private int boxBorderColor = Color.DKGRAY;
        private int boxFillColor = Color.GRAY;

        private RouterBoxerData(String message) {
            this.message = message;
        }

        private RouterBoxerData(ArrayList<RouteBoxer.Box> boxes, int boxBorderColor, int boxFillColor) {
            this.boxes = boxes;
            this.type = DataType.Boxes;
            this.boxBorderColor = boxBorderColor;
            this.boxFillColor = boxFillColor;
        }

    }

    private enum DataType {
        Message, Boxes
    }

    public interface IRouteBoxerTask {

        void onRouteBoxerTaskComplete(ArrayList<RouteBoxer.Box> boxes);
        void onRouteBoxerMessage(String message);
        void onRouteBoxerBoxes(ArrayList<RouteBoxer.Box> boxes, int boxBorderColor, int boxFillColor);
    }
}
