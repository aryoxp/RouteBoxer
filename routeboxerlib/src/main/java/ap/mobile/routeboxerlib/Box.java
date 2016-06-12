package ap.mobile.routeboxerlib;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aryo on 30/1/16.
 */
public class Box {

    public int x;
    public int y;

    public Boolean marked = false;
    public Boolean expandMarked = false;
    public Boolean merged = false;

    public LatLng ne;
    public LatLng sw;

    public Box() {}

    public Box(LatLng sw, LatLng ne, int x, int y) {
        this.sw = sw;
        this.ne = ne;
        this.x = x;
        this.y = y;
    }

    public Box mark() { this.marked = true; return this; }
    public Box unmark() { this.marked = false; return this; }
    public Box expandMark() { this.expandMarked = true; return this; }
    public Box unexpandMark() { this.expandMarked = false; return this; }

    public Box copy(Box box) {
        Box b = new Box();
        b.x = box.x;
        b.y = box.y;
        b.marked = box.marked;
        b.expandMarked = box.expandMarked;
        b.merged = box.merged;
        b.ne = new LatLng(box.ne.latitude, box.ne.longitude);
        b.sw = new LatLng(box.sw.latitude, box.sw.latitude);
        return b;
    }

}
