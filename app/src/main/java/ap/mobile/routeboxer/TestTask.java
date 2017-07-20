package ap.mobile.routeboxer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ap.mobile.routeboxer.helper.FileHelper;
import ap.mobile.routeboxerlib.RouteBoxer;

/**
 * Created by aryo on 13/6/16.
 */
public class TestTask extends AsyncTask<Void, String, Void> {

    private final ArrayList<LatLng> hPoints;
    private final ArrayList<LatLng> points;
    private final Context context;
    private final ArrayList<LatLng> dPoints;
    private TestInterface testInterface;

    public TestTask(Context context, ArrayList<LatLng> points, ArrayList<LatLng> dPoints, ArrayList<LatLng> hPoints) {
        this.context = context;
        this.points = points;
        this.dPoints = dPoints;
        this.hPoints = hPoints;
    }

    public interface TestInterface {
        void start();
        void showStatus(String status);
        void showError(String error);
        void done();
    }

    public void setStatusInterface(TestInterface testInterface) {
        this.testInterface = testInterface;
    }

    @Override
    protected void onPreExecute() {
        if(this.testInterface != null)
            this.testInterface.start();
    }

    @Override
    protected Void doInBackground(Void... params) {
        int distance = 20;
        int arraySize = 100;

        for(int i = distance; i<300; i+=10 ) {
            distance = i;
            this.publishProgress("Processing V-Distance " + i);
            ArrayList<LatLng> sublist = new ArrayList<>(points.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "v-distance-4~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "V-Distance " + i);
            this.publishProgress("V-Distance " + i);
        }
        Log.d("RouteBoxer", "V-Distance to Boxsize and Time, Done");
        this.publishProgress("V-Distance to Boxsize and Time, Done");

        //Toast.makeText(this, "V-Distance to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();

        distance = 20;

        for(int i = distance; i<300; i+=10 ) {
            this.publishProgress("Processing D-Distance " + i);
            distance = i;
            ArrayList<LatLng> sublist = new ArrayList<>(dPoints.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "d-distance-4~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "D-Distance " + i);
            this.publishProgress("D-Distance " + i);
        }
        Log.d("RouteBoxer", "D-Distance to Boxsize and Time, Done");
        this.publishProgress("D-Distance to Boxsize and Time, Done");
        //Toast.makeText(this, "D-Distance to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();


        distance = 20;

        for(int i = distance; i<300; i+=10 ) {
            this.publishProgress("Processing H-Distance " + i);
            distance = i;
            ArrayList<LatLng> sublist = new ArrayList<>(hPoints.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "h-distance-4~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "H-Distance " + i);
            this.publishProgress("H-Distance " + i);
        }
        Log.d("RouteBoxer", "H-Distance to Boxsize and Time, Done");
        this.publishProgress("H-Distance to Boxsize and Time, Done");
        //Toast.makeText(this, "H-Distance to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();


        distance = 30;

        for(int i = 10; i<100; i+=5 ) {
            this.publishProgress("Processing V-Numpath " + i);
            arraySize = i;
            ArrayList<LatLng> sublist = new ArrayList<>(points.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "v-numpath-25~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "V-Numpath " + i);
            this.publishProgress("V-Numpath " + i);
        }
        Log.d("RouteBoxer", "V-Numpath to Boxsize and Time, Done");
        this.publishProgress("V-Numpath to Boxsize and Time, Done");
        //Toast.makeText(this, "V-Numpath to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();

        distance = 30;

        for(int i = 10; i<100; i+=5 ) {
            this.publishProgress("Processing D-Numpath " + i);
            arraySize = i;
            ArrayList<LatLng> sublist = new ArrayList<>(dPoints.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "d-numpath-25~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "D-Numpath" + i);
            this.publishProgress("D-Numpath " + i);
        }
        Log.d("RouteBoxer", "D-Numpath to Boxsize and Time, Done");
        this.publishProgress("D-Numpath to Boxsize and Time, Done");
        //Toast.makeText(this, "D-Numpath to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();

        distance = 30;

        for(int i = 10; i<100; i+=5 ) {
            this.publishProgress("Processing H-Numpath " + i);
            arraySize = i;
            ArrayList<LatLng> sublist = new ArrayList<>(hPoints.subList(0, arraySize));
            RouteBoxer rb = new RouteBoxer(sublist, distance);
            double start = System.nanoTime();
            ArrayList<RouteBoxer.Box> boxes = rb.box();
            double time = System.nanoTime() - start;
            int size = boxes.size();
            int sizeH = rb.getRouteBoxesH().size();
            int sizeV = rb.getRouteBoxesV().size();
            String filename = "h-numpath-25~100+2-to-boxsize-and-time.txt";
            String data = distance + ";" + size + ";" + sizeH + ";" + sizeV + ";" + time + "\n";
            try {
                FileHelper.write(this.context, filename, data, false);
            } catch (Exception ex) {
                Log.e("RouteBoxer", "Unable to write file: " + filename);
                this.publishProgress(null, "Error: " + "Unable to write file: " + filename);
            }
            Log.d("RouteBoxer", "H-Numpath" + i);
            this.publishProgress("H-Numpath " + i);
        }
        Log.d("RouteBoxer", "H-Numpath to Boxsize and Time, Done");
        this.publishProgress("H-Numpath to Boxsize and Time, Done");
        //Toast.makeText(this, "H-Numpath to Boxsize and Time, Done", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(this.testInterface == null) return;

        if(values[0] != null)
            this.testInterface.showStatus(values[0]);
        else if(values[0] == null && values[1] != null)
            this.testInterface.showError(values[1]);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(this.testInterface != null)
            this.testInterface.done();
    }
}
