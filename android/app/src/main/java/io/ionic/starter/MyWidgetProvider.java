package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Instantiate and map our customized XML layout bundle
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}