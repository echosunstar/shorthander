package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private String pendingDay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());

        // Bind our native data engine directly to the Vanilla Web Window context
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new WidgetBridge(), "AndroidWidgetBridge");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        
        // If app is already active in memory, poke the web layout to parse updates
        WebView webView = getBridge().getWebView();
        if (webView != null) {
            webView.post(() -> webView.evaluateJavascript("if(window.checkWidgetTrigger) window.checkWidgetTrigger();", null));
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("target_day")) {
            pendingDay = intent.getStringExtra("target_day");
        }
    }

    // Expose clean endpoints that window.AndroidWidgetBridge can invoke inside index.html
    public class WidgetBridge {
        @JavascriptInterface
        public String getPendingDay() {
            String day = pendingDay;
            pendingDay = ""; // Consume string token immediately
            return day;
        }

        @JavascriptInterface
        public String getDayText(String day) {
            SharedPreferences prefs = getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
            return prefs.getString(day, "");
        }

        @JavascriptInterface
        public void saveDayText(String day, String text) {
            SharedPreferences prefs = getSharedPreferences("ScratchPadPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString(day, text).apply();

            // Broadcast real-time refresh signal to the desktop widget engine
            Context context = getApplicationContext();
            Intent intent = new Intent(context, MyWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }

        @JavascriptInterface
            public void goHome() {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
    }
}