package com.cam2.ryandevlin.worldview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Hussni on 3/22/2018.
 * This activity launches webview for url's sent to it
 */

public class web_cam extends AppCompatActivity {

    private static final String TAG = "Web_cam";
    private ProgressDialog mDialog;
    private VideoView videoView;
    private ImageButton btnPlayPause;

    String videoURL  = "rtsp://128.210.133.200/axis-cgi/mjpg/video.cgi";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_cam);

        // Configure the view that renders live video.
        videoView = (VideoView) findViewById(R.id.Web_view);


        if (videoURL.startsWith("rtsp://")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
            startActivity(intent);
        }

/*
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        //Set the path of Video or URI
        videoView.setVideoURI(Uri.parse("rtsp://tv.hindiworldtv.com:1935/live/getpnj"));
        //

        //Set the focus
        videoView.requestFocus();

*/

    }


}
