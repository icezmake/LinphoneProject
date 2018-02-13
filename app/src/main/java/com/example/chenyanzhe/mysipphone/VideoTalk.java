package com.example.chenyanzhe.mysipphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.linphone.core.LinphoneCore;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;


public class VideoTalk extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView mVideoView;
    private SurfaceView mCaptureView;
    private AndroidVideoWindowImpl androidVideoWindowImpl;
    private GestureDetector mGestureDetector;
    private float mZoomFactor = 1.f;
    private float mZoomCenterX, mZoomCenterY;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        mVideoView = (SurfaceView)findViewById(R.id.videoSurface);
        mCaptureView = (SurfaceView)findViewById(R.id.videoCaptureSurface);
        mCaptureView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Warning useless because value is ignored and automatically set by new APIs.

        fixZOrder(mVideoView, mCaptureView);
//        MyManager.getLc().setVideoWindow(mVideoView);
//        MyManager.getLc().setPreviewWindow(mCaptureView);



        androidVideoWindowImpl = new AndroidVideoWindowImpl(mVideoView, mCaptureView, new AndroidVideoWindowImpl.VideoWindowListener() {
            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                MyManager.getLc().setVideoWindow(vw);
                mVideoView = surface;
            }

            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                LinphoneCore lc = MyManager.getLc();
                if (lc != null) {
                    lc.setVideoWindow(null);
                }
            }

            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                mCaptureView = surface;
                MyManager.getLc().setPreviewWindow(mCaptureView);
            }

            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                // Remove references kept in jni code and restart camera
                MyManager.getLc().setPreviewWindow(null);
            }
        });
    }

    private void fixZOrder(SurfaceView video, SurfaceView preview) {
        //将两个视频窗口重叠
        video.setZOrderOnTop(false);
        preview.setZOrderOnTop(true);
        preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
    }

    public void switchCamera() {
        try {
            int videoDeviceId = MyManager.getLc().getVideoDevice();
            videoDeviceId = (videoDeviceId + 1) % AndroidCameraConfiguration.retrieveCameras().length;
            MyManager.getLc().setVideoDevice(videoDeviceId);
            // 这个是实现和配置文件
            //CallManager.getInstance().updateCall();

            // previous call will cause graph reconstruction -> regive preview
            // window
            if (mCaptureView != null) {
                MyManager.getLc().setPreviewWindow(mCaptureView);
            }
        } catch (ArithmeticException ae) {
            Log.e("Cannot swtich camera : no camera");
        }
    }


    @Override
    public void onClick(View v) {

    }
}
