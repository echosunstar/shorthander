package io.ionic.starter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);

            // 1. Pull current entries and append to views
            views.setTextViewText(R.id.text_yesterday, prefs.getString("yesterday", "Empty"));
            views.setTextViewText(R.id.text_today, prefs.getString("today", "Empty"));
            views.setTextViewText(R.id.text_tomorrow, prefs.getString("tomorrow", "Empty"));

            // 2. Wire intent clicks for each cell segment independently
            views.setOnClickPendingIntent(R.id.cell_yesterday, createClickIntent(context, "yesterday", 101));
            views.setOnClickPendingIntent(R.id.cell_today, createClickIntent(context, "today", 102));
            views.setOnClickPendingIntent(R.id.cell_tomorrow, createClickIntent(context, "tomorrow", 103));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent createClickIntent(Context context, String day, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("target_day", day);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}