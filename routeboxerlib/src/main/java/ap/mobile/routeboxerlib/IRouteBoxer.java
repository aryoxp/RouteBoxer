package ap.mobile.routeboxerlib;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

/**
 * Created by aryo on 30/1/16.
 */
public interface IRouteBoxer {

    void onBoxesObtained(ArrayList<Box> boxes);
    void onBoundsObtained(LatLngBounds bounds);
    void onGridObtained(Box[][] boxArray);
    void onGridMarked(ArrayList<Box> boxes);
    void onGridMarksExpanded(Box[][] boxArray);
    void onMergedVertically(ArrayList<Box> mergedBoxes);
    void onMergedHorizontally(ArrayList<Box> mergedBoxes);

}
