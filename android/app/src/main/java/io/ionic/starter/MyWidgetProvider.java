package io.ionic.starter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_PREV = "io.ionic.starter.ACTION_PREV";
    private static final String ACTION_NEXT = "io.ionic.starter.ACTION_NEXT";
    private static final String ACTION_RESET = "io.ionic.starter.ACTION_RESET"; // New Reset Target

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
        int offset = prefs.getInt("widget_offset", 0);

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat labelFormat = new SimpleDateFormat("d-MMM-yy", Locale.US);

        String[] keys = new String[3];
        String[] labels = new String[3];

        for (int i = 0; i < 3; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, offset + (i - 1));
            keys[i] = keyFormat.format(cal.getTime());
            labels[i] = labelFormat.format(cal.getTime()).toLowerCase();
        }

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);

            views.setTextViewText(R.id.label_yesterday, labels[0]);
            views.setTextViewText(R.id.text_yesterday, prefs.getString(keys[0], ""));

            views.setTextViewText(R.id.label_today, labels[1]);
            views.setTextViewText(R.id.text_today, prefs.getString(keys[1], ""));

            views.setTextViewText(R.id.label_tomorrow, labels[2]);
            views.setTextViewText(R.id.text_tomorrow, prefs.getString(keys[2], ""));

            views.setOnClickPendingIntent(R.id.cell_yesterday, createClickIntent(context, keys[0], 101));
            views.setOnClickPendingIntent(R.id.cell_today, createClickIntent(context, keys[1], 102));
            views.setOnClickPendingIntent(R.id.cell_tomorrow, createClickIntent(context, keys[2], 103));

            // Wire Navigation Actions
            views.setOnClickPendingIntent(R.id.btn_prev, createNavIntent(context, ACTION_PREV, 201));
            views.setOnClickPendingIntent(R.id.btn_next, createNavIntent(context, ACTION_NEXT, 202));
            views.setOnClickPendingIntent(R.id.btn_today, createNavIntent(context, ACTION_RESET, 203)); // Bind Reset Action

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (ACTION_PREV.equals(action) || ACTION_NEXT.equals(action) || ACTION_RESET.equals(action)) {
            SharedPreferences prefs = context.getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
            int offset = prefs.getInt("widget_offset", 0);

            if (ACTION_RESET.equals(action)) {
                offset = 0; // Snap cleanly back to today's anchor sequence
            } else if (ACTION_PREV.equals(action)) {
                if (offset > -2) offset--;
            } else if (ACTION_NEXT.equals(action)) {
                if (offset < 2) offset++;
            }

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