package ap.mobile.routeboxer;

import java.util.ArrayList;

import ap.mobile.routeboxerlib.Box;

/**
 * Created by aryo on 28/1/16.
 */
public interface IRouteBoxerTask {

    void onRouteBoxerTaskComplete(ArrayList<Box> boxes);
    void onRouteBoxerBoxPublished(ArrayList<Box> boxes);
}
