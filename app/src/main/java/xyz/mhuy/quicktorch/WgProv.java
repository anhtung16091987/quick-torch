package xyz.mhuy.quicktorch;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WgProv extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Intent receiver = new Intent(context, WgRec.class);
        receiver.setAction("COM_QUICKTORCH");
        receiver.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                receiver, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.flashlight_widget_imageview,
                pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}