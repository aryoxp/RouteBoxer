package ap.mobile.routeboxer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by aryo on 31/1/16.
 */
public class DistanceDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private DialogInterface.OnClickListener distanceDialogListener;
    private SeekBar seekbarDistance;
    private TextView textviewDistance;

    public float distance;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        View dialog = LayoutInflater.from(this.getActivity()).inflate(R.layout.dialog_distance, null);
        this.textviewDistance = (TextView) dialog.findViewById(R.id.textview_distance);
        this.seekbarDistance = (SeekBar) dialog.findViewById(R.id.seekbar_distance);
        this.seekbarDistance.setOnSeekBarChangeListener(this);
        this.seekbarDistance.setProgress((int)(Math.sqrt(this.distance / 2)));
        builder.setView(dialog)
                .setTitle("Box distance")
                .setPositiveButton("OK", this.distanceDialogListener)
                .setNegativeButton("Cancel", this.distanceDialogListener);
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        this.distanceDialogListener = (DialogInterface.OnClickListener) activity;
        super.onAttach(activity);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float distance = (float)progress * progress * 2;
        this.distance = distance;
        this.textviewDistance.setText(String.valueOf(distance + " m"));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    public void setDistance(int distance) {
        this.distance = (float)distance;
    }
}
