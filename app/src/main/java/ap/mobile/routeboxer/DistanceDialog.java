package ap.mobile.routeboxer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by aryo on 31/1/16.
 */
public class DistanceDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private DialogInterface.OnClickListener distanceDialogListener;
    private SeekBar seekbarDistance;
    private TextView textviewDistance;
    private CheckBox cbSimplify;

    public float distance;
    private boolean useSimplify = true;
    private boolean runBoth = false;
    private Context context;
    private CheckBox cbRunBoth;


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

        this.cbSimplify = (CheckBox)dialog.findViewById(R.id.check_simplify);
        this.cbSimplify.setChecked(this.useSimplify);
        this.cbSimplify.setOnCheckedChangeListener(this);

        this.cbRunBoth = (CheckBox)dialog.findViewById(R.id.check_runboth);
        this.cbRunBoth.setChecked(this.runBoth);
        this.cbRunBoth.setOnCheckedChangeListener(this);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        this.distanceDialogListener = (DialogInterface.OnClickListener) context;
        this.context = context;
        this.useSimplify = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_simplify", true);
        this.runBoth = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_runboth", false);
        super.onAttach(context);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch(compoundButton.getId()) {
            case R.id.check_simplify:
                PreferenceManager.getDefaultSharedPreferences(this.context).edit().putBoolean("pref_simplify", b).apply();
                break;
            case R.id.check_runboth:
                PreferenceManager.getDefaultSharedPreferences(this.context).edit().putBoolean("pref_runboth", b).apply();
                break;
        }
    }
}
