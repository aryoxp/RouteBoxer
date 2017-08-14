package ap.mobile.routeboxer;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by aryo on 28/1/16.
 */
public interface IMaps {
    void onJSONRouteLoaded(ArrayList<LatLng> route) throws IOException;
    void routeJsonObtained(String json);
}
