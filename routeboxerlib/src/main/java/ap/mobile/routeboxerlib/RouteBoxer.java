package ap.mobile.routeboxerlib;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

/**
 * Created by aryo on 29/1/16.
 */
public class RouteBoxer {

    private final ArrayList<LatLng> route;
    private final int distance;
    private LatLngBounds bounds;
    private IRouteBoxer iRouteBoxer;
    private ArrayList<Box> boxes = new ArrayList<>();
    private ArrayList<Box> routeBoxesH;
    private ArrayList<Box> routeBoxesV;

    public RouteBoxer(ArrayList<LatLng> route, int distance) {
        this.route = route;
        this.distance = distance;
    }

    public void setRouteBoxerInterface(IRouteBoxer iRouteBoxer) {
        this.iRouteBoxer = iRouteBoxer;
    }

    public static ArrayList<Box> box(ArrayList<LatLng> path, int distance) {
        RouteBoxer routeBoxer = new RouteBoxer(path, distance);
        return routeBoxer.box();
    }

    public ArrayList<Box> box() {

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Initializing...", null);


        double degree = distance / 1.1132 * 0.00001;

        // Getting bounds

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Calculating bounds...", null);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (LatLng point : this.route) {
            builder.include(point);
        }

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Expanding bounds...", null);

        // Expanding bounds

        this.bounds = builder.build();
        LatLng southwest = new LatLng(this.bounds.southwest.latitude - degree,
                this.bounds.southwest.longitude - degree);
        LatLng northeast = new LatLng(this.bounds.northeast.latitude + degree,
                this.bounds.northeast.longitude + degree);
        this.bounds = builder.include(southwest).include(northeast).build();

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Bounds obtained...", null);
            this.iRouteBoxer.onBoundsObtained(this.bounds);
        }

        // Laying out grids

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Overlaying grid...", null);

        LatLng sw = this.bounds.southwest;
        LatLng ne = new LatLng(sw.latitude + degree, sw.longitude + degree);
        int x = 0, y = 0;
        Box gridBox;

        do {

            do {
                gridBox = new Box(sw, ne, x, y);
                this.boxes.add(gridBox); //box.draw(mMap, Color.BLUE);
                sw = new LatLng(sw.latitude, ne.longitude);
                ne = new LatLng(sw.latitude + degree, sw.longitude + degree);
                x++;
            } while (gridBox.ne.longitude < this.bounds.northeast.longitude);

            if (gridBox.ne.latitude < this.bounds.northeast.latitude) {
                x = 0;
                sw = new LatLng(sw.latitude + degree, this.bounds.southwest.longitude);
                ne = new LatLng(sw.latitude + degree, sw.longitude + degree);
            }
            y++;

        } while (gridBox.ne.latitude < this.bounds.northeast.latitude);


        // Center the grids
        // and converts to 2-D array

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Overlaying grid...", null);

        double latDif = boxes.get(boxes.size() - 1).ne.latitude - this.bounds.northeast.latitude;
        double lngDif = boxes.get(boxes.size() - 1).ne.longitude - this.bounds.northeast.longitude;

        Box boxArray[][] = new Box[x][y];
        for (Box bx : boxes) {
            bx.sw = new LatLng(bx.sw.latitude - (latDif / 2), bx.sw.longitude - (lngDif / 2));
            bx.ne = new LatLng(bx.ne.latitude - (latDif / 2), bx.ne.longitude - (lngDif / 2));
            boxArray[bx.x][bx.y] = bx;
        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onGridObtained(boxArray, boxes);
            this.iRouteBoxer.onProcess("Traversing...", null);
        }

        // step 2: Traverse all points and mark grid which contains it.
        boxArray = this.traversePointsAndMarkGrids(boxArray);

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Expanding cell marks...", null);

        // step 3: Expand marked cells
        boxArray = this.expandMarks(x, y, boxArray);


        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Duplicating array...", null);

        int length = boxArray.length;
        Box[][] boxArrayCopy = new Box[length][boxArray[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(boxArray[i], 0, boxArrayCopy[i], 0, boxArray[i].length);
        }

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Merging horizontally...", null);
        // step 4: Merge cells and generate boxes
        // 1st Approach: merge cells horizontally
        this.horizontalMerge(x, y, boxArray);

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Merging vertically...", null);
        // 2nd Approach: Merge cells vertically
        this.verticalMerge(x, y, boxArrayCopy);

        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onProcess("Obtaining results...", null);
        // Step 5: return boxes with the least count from both approach
        ArrayList<Box> boxes = (this.routeBoxesV.size() >= routeBoxesH.size()) ? this.routeBoxesH : this.routeBoxesV;
        if(this.iRouteBoxer != null)
            this.iRouteBoxer.onBoxesObtained(boxes);

        return boxes;

    }

    private Box[][] traversePointsAndMarkGrids(Box[][] boxArray) {
        int sizeX = boxArray.length;
        int sizeY = boxArray[0].length;
        Box lastBox = null;
        for (LatLng point : this.route) {
            for(int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    Box bx = boxArray[x][y];
                    if(bx.marked) continue;
                    if (point.latitude > bx.sw.latitude
                            && point.latitude < bx.ne.latitude
                            && point.longitude > bx.sw.longitude
                            && point.longitude < bx.ne.longitude) {
                        bx.mark();
                        if (lastBox == null)
                            lastBox = bx;
                        else {
                            int lastX = lastBox.x;
                            int lastY = lastBox.y;
                            int diffX = bx.x - lastX;
                            int diffY = bx.y - lastY;
                            if(diffX < 1) {
                                for(int dx = bx.x - diffX - 1; dx > bx.x; dx--)
                                    boxArray[dx][lastBox.y].mark();
                            }
                            if(diffX > 1) {
                                for(int dx = bx.x - diffX + 1; dx < bx.x; dx++)
                                    boxArray[dx][lastBox.y].mark();
                            }
                            if(diffY < 1){
                                for(int dy = bx.y - diffY - 1; dy > bx.y; dy--)
                                    boxArray[lastBox.x][dy].mark();
                            }
                            if(diffY > 1) {
                                for(int dy = bx.y - diffY + 1; dy < bx.y; dy++)
                                    boxArray[lastBox.x][dy].mark();
                            }
                            lastBox = bx;
                        }
                    }
                }
            }
        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Traversing and marking complete...", null);
            this.iRouteBoxer.onGridMarked(this.boxes);
        }

        return boxArray;

    }

    private Box[][] expandMarks(int x, int y, Box[][] boxArray) {
        for(Box b:boxes) {
            if(b.marked) {

                // Mark all surrounding cells
                boxArray[b.x-1][b.y-1].expandMark();    //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x-1][b.y].expandMark();      //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x-1][b.y+1].expandMark();    //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x][b.y+1].expandMark();      //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x+1][b.y+1].expandMark();    //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x+1][b.y].expandMark();      //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x+1][b.y-1].expandMark();    //.redraw(mMap, Color.BLACK, Color.GREEN);
                boxArray[b.x][b.y-1].expandMark();      //.redraw(mMap, Color.BLACK, Color.GREEN);
            }
        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Cell marks expanded", null);
            this.iRouteBoxer.onGridMarksExpanded(boxArray, this.boxes);
        }

        return boxArray;
    }

    private void verticalMerge(int x, int y, Box[][] boxArray) {
        ArrayList<Box> mergedBoxes = new ArrayList<>();
        Box vBox = null;
        for(int cx = 0; cx < x; cx++) {
            for (int cy = 0; cy < y; cy++) {
                Box b = new Box(boxArray[cx][cy].sw, boxArray[cx][cy].ne, cx, cy);
                if(boxArray[cx][cy].marked || boxArray[cx][cy].expandMarked)
                    b.mark();
                if ((b.marked || b.expandMarked)) {
                    if (vBox == null)
                        vBox = b;
                    else vBox.ne = b.ne;
                    if(cy == y-1) {
                        vBox.unexpandMark().unmark();
                        mergedBoxes.add(vBox);
                        vBox = null;
                    }
                } else {
                    if(vBox != null) {
                        vBox.unexpandMark().unmark();
                        mergedBoxes.add(vBox);
                        vBox = null;
                    }
                }
            }
        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Adjoint cells merged.", null);
            this.iRouteBoxer.onMergedAdjointVertically(mergedBoxes);
        }



        this.routeBoxesV = new ArrayList<>();
        Box rBox = null;
        for(int i = 0; i < mergedBoxes.size(); i++) {
            Box bx = mergedBoxes.get(i);
            if(bx.merged)
                continue;
            rBox = bx;
            for (int j = i + 1; j < mergedBoxes.size(); j++) {
                Box b = mergedBoxes.get(j);
                if (b.sw.latitude == rBox.sw.latitude
                        && b.ne.latitude == rBox.ne.latitude
                        && b.sw.longitude == rBox.ne.longitude) {
                    rBox.ne = b.ne;
                    b.merged = true;
                }
            }
            routeBoxesV.add(rBox);

        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Adjoint boxes merged.", null);
            this.iRouteBoxer.onMergedVertically(routeBoxesV);
        }
    }

    private void horizontalMerge(int x, int y, Box[][] boxArray) {
        ArrayList<Box> mergedBoxes = new ArrayList<>();
        Box hBox = null;
        for(int cy = 0; cy < y; cy++) {
            for (int cx = 0; cx < x; cx++) {
                Box b = new Box(boxArray[cx][cy].sw, boxArray[cx][cy].ne, cx, cy);
                if(boxArray[cx][cy].marked || boxArray[cx][cy].expandMarked)
                    b.mark();
                if ((b.marked || b.expandMarked)) {
                    if (hBox == null)
                        hBox = b;
                    else hBox.ne = b.ne;
                    if(cx == x-1) {
                        hBox.unexpandMark().unmark();
                        mergedBoxes.add(hBox);
                        hBox = null;
                    }
                } else {
                    if(hBox != null) {
                        hBox.unexpandMark().unmark();
                        mergedBoxes.add(hBox);
                        hBox = null;
                    }
                }
            }
        }

        if(this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Adjoint cells merged.", null);
            this.iRouteBoxer.onMergedAdjointHorizontally(mergedBoxes);
        }

        this.routeBoxesH = new ArrayList<>();
        Box rBox = null;
        for(int i = 0; i < mergedBoxes.size(); i++) {
            Box bx = mergedBoxes.get(i);
            if(bx.merged)
                continue;
            rBox = bx;
            for (int j = i + 1; j < mergedBoxes.size(); j++) {
                Box b = mergedBoxes.get(j);
                if (b.sw.longitude == rBox.sw.longitude
                        && b.sw.latitude == rBox.ne.latitude
                        && b.ne.longitude == rBox.ne.longitude) {
                    rBox.ne = b.ne;
                    b.merged = true;
                }
            }
            routeBoxesH.add(rBox);
        }

        if (this.iRouteBoxer != null) {
            this.iRouteBoxer.onProcess("Adjoint boxes merged.", null);
            this.iRouteBoxer.onMergedHorizontally(routeBoxesH);
        }
    }

    public ArrayList<Box> getRouteBoxesH() {
        return this.routeBoxesH;
    }
    public ArrayList<Box> getRouteBoxesV() {
        return this.routeBoxesV;
    }

    /**
     * Created by aryo on 30/1/16.
     */
    public static interface IRouteBoxer {

        void onBoxesObtained(ArrayList<Box> boxes);
        void onBoundsObtained(LatLngBounds bounds);
        void onGridObtained(Box[][] boxArray, ArrayList<Box> boxes);
        void onGridMarked(ArrayList<Box> boxes);
        void onGridMarksExpanded(Box[][] boxArray, ArrayList<Box> boxes);
        void onMergedAdjointVertically(ArrayList<Box> boxes);
        void onMergedAdjointHorizontally(ArrayList<Box> boxes);
        void onMergedVertically(ArrayList<Box> mergedBoxes);
        void onMergedHorizontally(ArrayList<Box> mergedBoxes);
        void onProcess(String processInfo, ArrayList<Box> boxes);
    }

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


}
