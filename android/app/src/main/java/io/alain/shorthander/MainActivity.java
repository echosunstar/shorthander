package io.alain.shorthander;
import io.alain.shorthander.R;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import android.view.inputmethod.InputMethodManager;

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

    // Locate and replace the handleIntent method inside MainActivity.java
    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("target_day")) {
            pendingDay = intent.getStringExtra("target_day");
        } else {
            pendingDay = ""; // Launcher icon tap explicitly clears out target focus tokens
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
        public void forceSystemKeyboard() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // SHOW_FORCED bypasses all WebView user-gesture validation architectures completely
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            });
        }

        @JavascriptInterface
        public void saveDayText(String dateKey, String textContent) {
            Context context = getApplicationContext();

            // 📢 TRACER 1: Verify the web layout is actually passing data over the bridge
            //android.util.Log.d("SHORTHANDER", "➡️ WEB WRITE -> Key: " + dateKey + " | Value: " + textContent);
            
            // 1. Write your input stream securely to your hardware AES256 vault
            EncryptedStorage.write(context, dateKey, textContent);
            
            // 2. Query the OS for every active instance of your widget pinned to the workspace
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
            
            // 3. Build the update intent and attach the active IDs payload
            Intent updateIntent = new Intent(context, MyWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            
            // 4. Fire the broadcast to force the onUpdate rendering loops to execute
            context.sendBroadcast(updateIntent);
        }

        @JavascriptInterface
        public String getDayText(String dateKey) {
            // Transparently decrypts the value on-the-fly straight to the web layout buffer
            return EncryptedStorage.read(getApplicationContext(), dateKey);
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