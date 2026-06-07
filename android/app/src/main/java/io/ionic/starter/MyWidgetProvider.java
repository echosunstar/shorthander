package io.ionic.starter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_PREV = "io.ionic.starter.ACTION_PREV";
    private static final String ACTION_NEXT = "io.ionic.starter.ACTION_NEXT";
    private static final String ACTION_RESET = "io.ionic.starter.ACTION_RESET";

    // Element Resource Array Layout Maps
    private final int[] cellIds = {R.id.cell_1, R.id.cell_2, R.id.cell_3, R.id.cell_4, R.id.cell_5, R.id.cell_6};
    private final int[] labelIds = {R.id.label_1, R.id.label_2, R.id.label_3, R.id.label_4, R.id.label_5, R.id.label_6};
    private final int[] textIds = {R.id.text_1, R.id.text_2, R.id.text_3, R.id.text_4, R.id.text_5, R.id.text_6};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
        int offset = prefs.getInt("widget_offset", 0);

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat labelFormat = new SimpleDateFormat("d-MMM-yy", Locale.US);
        
        // Target absolute string key identifier for actual current date
        String realTodayKey = keyFormat.format(Calendar.getInstance().getTime());

        for (int appWidgetId : appWidgetIds) {
            // Determine dynamic aspect width bounds from option map metrics
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            
            boolean isWide = (minWidth >= 220); 
            int visibleCellsCount = isWide ? 6 : 3;

            int layoutId = isWide ? R.layout.my_widget_layout_wide : R.layout.my_widget_layout;
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

            // Populate active timeline slots dynamically
            for (int i = 0; i < visibleCellsCount; i++) {
                Calendar cal = Calendar.getInstance();
                // Slot index offset: narrow spans i-1 (-1, 0, 1), wide extends straight to 4 days ahead
                cal.add(Calendar.DAY_OF_YEAR, offset + (i - 1));

                String currentKey = keyFormat.format(cal.getTime());
                String labelText = labelFormat.format(cal.getTime()).toLowerCase();

                views.setTextViewText(labelIds[i], labelText);
                views.setTextViewText(textIds[i], prefs.getString(currentKey, ""));
                views.setOnClickPendingIntent(cellIds[i], createClickIntent(context, currentKey, 100 + i));

                // --- ERGONOMIC HIGHLIGHTING ENGINE ---
                // Paint actual today a distinct dark sapphire blue hue so it pops instantly
                if (currentKey.equals(realTodayKey)) {
                    views.setInt(cellIds[i], "setBackgroundColor", Color.parseColor("#1f385c"));
                } else {
                    // Reset to default column tints
                    if (isWide) {
                        views.setInt(cellIds[i], "setBackgroundColor", (i < 3) ? Color.parseColor("#1a1a26") : Color.parseColor("#212130"));
                    } else {
                        views.setInt(cellIds[i], "setBackgroundColor", (i == 1) ? Color.parseColor("#252538") : Color.parseColor("#1e1e2e"));
                    }
                }
            }

            // Wire control bar hooks
            views.setOnClickPendingIntent(R.id.btn_prev, createNavIntent(context, ACTION_PREV, 201));
            views.setOnClickPendingIntent(R.id.btn_next, createNavIntent(context, ACTION_NEXT, 202));
            views.setOnClickPendingIntent(R.id.btn_today, createNavIntent(context, ACTION_RESET, 203));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        // Force an immediate evaluation check loop when a resizing constraint is fired
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }


    // Locate the onReceive method inside your MyWidgetProvider.java and update its condition blocks:
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        // Catch manual widget clicks OR system midnight date changes/manual clock adjustments
        if (ACTION_PREV.equals(action) || ACTION_NEXT.equals(action) || ACTION_RESET.equals(action) ||
            Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action)) {
            
            SharedPreferences prefs = context.getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
            int offset = prefs.getInt("widget_offset", 0);

            if (ACTION_RESET.equals(action)) {
                offset = 0;
            } else if (ACTION_PREV.equals(action)) {
                if (offset > -4) offset--;
            } else if (ACTION_NEXT.equals(action)) {
                if (offset < 4) offset++;
            }
            // System date rollover actions pass through silently, maintaining the relative date mapping anchors

            prefs.edit().putInt("widget_offset", offset).apply();

            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
            onUpdate(context, mgr, ids);
        }
    }

    private PendingIntent createClickIntent(Context context, String dateKey, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("target_day", dateKey);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    private PendingIntent createNavIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }
}