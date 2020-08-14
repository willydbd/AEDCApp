/**
 * @author
 * Banjo Mofesola Paul
 * Chief Developer, Planet NEST
 * mofesolapaul@live.com
 * Thursday, April 28, 2016
 */

package org.planetnest.aedcapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.Calendar;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ScriptInterface {
    public static WebView Browser;
    public static Context context;
    public static SharedPreferences settings;
    public static Editor settings_editor;
    public String appWhere = "";
    private String interpageData = "";
    public boolean showing_result = false;

    public ScriptInterface(Context ctx, WebView webView) {
        context = ctx;
        Browser = webView;
        settings = ctx.getSharedPreferences("AEDCAppSettings", Context.MODE_PRIVATE);
        settings_editor = settings.edit();
    }

    public static void Execute(String js) {
        Browser.loadUrl("javascript:" + js);
    }

    @JavascriptInterface
    public void clearNavHistory() {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            public void run() {
                ScriptInterface.Browser.clearHistory();
            }
        });
    }

    @JavascriptInterface
    public DatabaseHelper db()
    {
        return ((MainActivity)context).myDbHelper;
    }

    @JavascriptInterface
    public void log(String msg) {
        Log.w("APP LOG", msg);
    }

    @JavascriptInterface
    public void setAppWhere(String appWhere) {
        this.appWhere = appWhere;
    }

    @JavascriptInterface
    public String getInterpageData() {
        return this.interpageData;
    }

    @JavascriptInterface
    public void setInterpageData(String payload) {
        this.interpageData = payload;
    }

    @JavascriptInterface
    public String getSettings(String label, String value) {
        return settings.getString(label, value);
    }

    @JavascriptInterface
    public void setSettings(String label, String value) {
        settings_editor.putString(label, value);
    }

    @JavascriptInterface
    public void saveSettings() {
        settings_editor.apply();
    }

    @JavascriptInterface
    public void swal(String msg, String type) {
        int dialogType =
                (type.equals("error"))? SweetAlertDialog.ERROR_TYPE:(
                        (type.equals("success"))? SweetAlertDialog.SUCCESS_TYPE:(
                                (type.equals("warn"))? SweetAlertDialog.WARNING_TYPE:SweetAlertDialog.NORMAL_TYPE
                                )
                        );
        new SweetAlertDialog(context, dialogType)
                .setTitleText("")
                .setContentText(msg)
                .show();
    }

    @JavascriptInterface
    public void swal(String title, String msg, String type) {
        int dialogType =
                (type.equals("error"))? SweetAlertDialog.ERROR_TYPE:(
                        (type.equals("success"))? SweetAlertDialog.SUCCESS_TYPE:(
                                (type.equals("warn"))? SweetAlertDialog.WARNING_TYPE:SweetAlertDialog.NORMAL_TYPE
                        )
                );
        new SweetAlertDialog(context, dialogType)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    @JavascriptInterface
    public void showForecast() {
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Forecast")
                .setContentText("Leave me alone jee!")
                .show();
    }

}