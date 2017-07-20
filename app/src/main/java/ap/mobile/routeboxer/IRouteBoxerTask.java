package ap.mobile.routeboxer;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.RouteBoxer;


/**
 * Created by aryo on 28/1/16.
 */
public interface IRouteBoxerTask {

    void onRouteBoxerTaskComplete(ArrayList<RouteBoxer.Box> boxes);
    void onRouteBoxerBoxPublished(ArrayList<RouteBoxer.Box> boxes, int step);
    void onMessage(String message);
}
