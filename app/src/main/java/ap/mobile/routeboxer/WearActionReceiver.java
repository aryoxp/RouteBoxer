package ap.mobile.routeboxer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class WearActionReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID_STRING = "NotificationId";
    public static final String WEAR_ACTION = "routeboxer.wearaction";
    public static final String WEAR_ACTION_CODE = "routeboxer.wearaction.action";
    public static final int DISMISS_NOTIFICATION = 1;
    public static final int RECALCULATE = 2;

    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d("BC", "Broadcast received");
        if (intent != null) {

            int notificationId = intent.getIntExtra(NOTIFICATION_ID_STRING, 0);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);

            int action = intent.getIntExtra(WEAR_ACTION_CODE, 0);
            switch (action) {
                case DISMISS_NOTIFICATION:
                    this.onDismiss();
                    break;
                case RECALCULATE:
                    this.onRecalculate();
                    break;
                default:
                    break;
            }

        }
    }

    public abstract void onDismiss();
    public abstract void onRecalculate();
}