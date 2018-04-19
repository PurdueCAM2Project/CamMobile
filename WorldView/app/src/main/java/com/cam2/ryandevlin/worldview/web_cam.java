package com.cam2.ryandevlin.worldview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Hussni on 3/22/2018.
 * This activity launches webview for url's sent to it
 */

public class web_cam extends AppCompatActivity {
    private static final String TAG = "Web_cam";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_cam);
        //get Intent
        Intent Incomingintent = getIntent();
        // get URL
        String weburl = Incomingintent.getStringExtra("source");
        // Launch Webview
        WebView webb = (WebView) findViewById(R.id.Web_view);
        WebSettings webSettings = webb.getSettings();
        //This setting allows Videoplayer in website to run
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webb.setWebViewClient(new WebViewClient());
        webb.loadUrl(weburl);
    }

}
