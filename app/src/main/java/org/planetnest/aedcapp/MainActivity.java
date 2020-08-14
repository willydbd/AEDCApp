/**
 * @author
 * Banjo Mofesola Paul
 * Chief Developer, Planet NEST
 * mofesolapaul@live.com
 * Thursday, April 28, 2016
 */

package org.planetnest.aedcapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends Activity {
    static MainActivity Me;
    Context context;
    WebView mainView;
    DatabaseHelper myDbHelper;
    ScriptInterface scriptInterface;

    private void init() {
        Me = this;
        this.context = getBaseContext();
        this.mainView = ((WebView) findViewById(R.id.webView));

        WebSettings setting = this.mainView.getSettings();
        setting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setting.setSaveFormData(false);
        setting.setSupportZoom(false);
        setting.setLoadsImagesAutomatically(true);
        setting.setAllowFileAccess(true);
        setting.setAppCacheEnabled(true);
        setting.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            fixNewAndroid(mainView);
        }

        this.mainView.setWebChromeClient(new MyWebChromeClient());
        this.scriptInterface = new ScriptInterface(this, this.mainView);
        this.mainView.addJavascriptInterface(this.scriptInterface, "Android");

        this.mainView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("mailto:")) {
                    view.loadUrl(url);
                    return false;
                }

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
        });
        this.mainView.loadUrl("file:///android_asset/www/index.htm");
    }

    @TargetApi(16)
    protected void fixNewAndroid(WebView paramWebView) {
        try {
            mainView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        } catch (NullPointerException e) {
        }
    }

    public void onBackPressed() {
        boolean canExit = false;
        if (this.mainView.canGoBack()) this.mainView.goBack();
        else canExit = true;
        if (canExit) super.onBackPressed();
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);

        // db init
        myDbHelper = new DatabaseHelper(this);
        myDbHelper.createDataBase();
        myDbHelper.openDataBase();

        init();
    }

    public boolean onCreateOptionsMenu(Menu paramMenu) {
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mainView.loadUrl("about:blank");
        this.mainView.stopLoading();
        this.mainView.setWebChromeClient(null);
        this.mainView.setWebViewClient(null);
        ((RelativeLayout) findViewById(R.id.mainLayout)).removeView(this.mainView);
        this.mainView.removeAllViews();
        this.mainView.destroy();
        this.mainView = null;
    }

    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        if (paramMenuItem.getItemId() == 2131230722) {
            return true;
        }
        return super.onOptionsItemSelected(paramMenuItem);
    }

    @SuppressLint({"NewApi"})
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT > 8) {
            this.mainView.onPause();
        }

        this.mainView.pauseTimers();
        try {
            WebView.class.getMethod("onPause", new Class[0]).invoke(this.mainView, new Object[0]);
        } catch (IllegalAccessException localIllegalAccessException) {
            localIllegalAccessException.printStackTrace();
        } catch (InvocationTargetException localInvocationTargetException) {
            localInvocationTargetException.printStackTrace();
        } catch (NoSuchMethodException localNoSuchMethodException) {
            localNoSuchMethodException.printStackTrace();
        }
    }

    @SuppressLint({"NewApi"})
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT > 8) {
            this.mainView.onResume();
        }

        this.mainView.resumeTimers();
        try {
            WebView.class.getMethod("onResume", new Class[0]).invoke(this.mainView, new Object[0]);
        } catch (IllegalAccessException localIllegalAccessException) {
            localIllegalAccessException.printStackTrace();
        } catch (InvocationTargetException localInvocationTargetException) {
            localInvocationTargetException.printStackTrace();
        } catch (NoSuchMethodException localNoSuchMethodException) {
            localNoSuchMethodException.printStackTrace();
        }

    }

    final class MyWebChromeClient extends WebChromeClient {
        MyWebChromeClient() {
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Information")
                    .setMessage(message)
                    .setPositiveButton("Ok",
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do your stuff here
                                    result.confirm();
                                }
                            }).setCancelable(false).create().show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirm")
                    .setMessage(message)
                    .setPositiveButton("Ok",
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do your stuff here
                                    result.confirm();
                                }
                            }).setCancelable(false).create().show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, final JsPromptResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Prompt")
                    .setMessage(message)
                    .setPositiveButton("Ok",
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do your stuff here
                                    result.confirm();
                                }
                            }).setCancelable(false).create().show();
            return true;
        }
    }
}