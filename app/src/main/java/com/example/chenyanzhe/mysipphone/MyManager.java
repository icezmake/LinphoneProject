package com.example.chenyanzhe.mysipphone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.widget.Toast;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PayloadType;
import org.linphone.core.PublishState;
import org.linphone.core.Reason;
import org.linphone.core.SubscriptionState;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.Version;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MyManager implements LinphoneCoreListener {
    public static MyManager mInstance;
    private Context mContext;
    private static LinphoneCore mLinphoneCore;
    private Timer mTimer;
    private LinphoneCoreListener mListener;
    private LinphoneCall mCall;
    private AudioManager mAudioManager;
    private PowerManager mPowerManager;
    private Resources mR;
    private ConnectivityManager mConnectivityManager;
    private Vibrator mVibrator;

    public MyManager(Context c) {
        mContext = c;
        LinphoneCoreFactory.instance().setDebugMode(true, "myphone");

        mAudioManager = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
        mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        mConnectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        mR = c.getResources();


        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            copyAssetsFromPackage(basePath);
            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);
            initLinphoneCoreValues(basePath);

            setUserAgent();
            setFrontCamAsDefault();
            startIterate();
            mInstance = this;
            mLinphoneCore.setNetworkReachable(true); // 假设网络已经通了

        } catch (LinphoneCoreException e) {
        } catch (IOException e) {
        }

    }

    public static LinphoneCore getInstance() {
        return mLinphoneCore;
    }
    public static MyManager getmInstance(){return mInstance;}


    public static void login(String account,String password){
        String strDomain = "sip.linphone.org";
        String strIdetify = "sip:"+account+"@" + strDomain;

        LinphoneProxyConfig proxyCfg = null;
        try {
            proxyCfg = mLinphoneCore.createProxyConfig(strIdetify, strDomain, null, true);
            proxyCfg.setExpires(300);
            mLinphoneCore.addProxyConfig(proxyCfg);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(
                    account, password, null, strDomain);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(proxyCfg);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public void invite(String url){
        try {
            LinphoneAddress la = LinphoneCoreFactory.instance().createLinphoneAddress(url);
            la.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            mCall = mLinphoneCore.invite(la);
        } catch (LinphoneCoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void videoCall(String to){
        if (to == null) return;
        LinphoneAddress la = null;
        try {
            la = LinphoneCoreFactory.instance().createLinphoneAddress(to);
            la.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            LinphoneCallParams params = mLinphoneCore.createCallParams(null);
            params.setVideoEnabled(true);
            params.enableLowBandwidth(true);
            mLinphoneCore.inviteAddressWithParams(la, params);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public void hangUp(){
        if(mCall!=null) {
            mLinphoneCore.terminateCall(mCall);
        }
    }

    public void declineCall(){
        if(mCall.getState() == LinphoneCall.State.IncomingReceived){
            mLinphoneCore.declineCall(mCall,Reason.Declined);
        }
    }

    public void acceptCall() throws LinphoneCoreException{
        List address = MyUtils.getLinphoneCalls(mLinphoneCore);
        Iterator contact = address.iterator();

        while (contact.hasNext()) {
            mCall = (LinphoneCall) contact.next();
            if (LinphoneCall.State.IncomingReceived == mCall.getState()) {
                break;
            }
        }

        if (mCall == null) {
            Log.e("Couldn\'t find incoming call");
        } else {
            LinphoneCallParams params = mLinphoneCore.createCallParams(mCall);
            params.enableLowBandwidth(false);

            LinphoneAddress address1 = mCall.getRemoteAddress();
            Log.d( "Find a incoming call, number: " + address1.asStringUriOnly());
            try {
                mLinphoneCore.acceptCallWithParams(mCall, params);
            } catch (LinphoneCoreException e) {
                Log.e( "Accept Call exception: ", e);
            }
        }
    }

    private void copyAssetsFromPackage(String basePath) throws IOException {
        MyUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        MyUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
        MyUtils.copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
        MyUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
        MyUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        MyUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        MyUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
    }

    private void initLinphoneCoreValues(String basePath) {
        mLinphoneCore.setContext(mContext);
        mLinphoneCore.setRing(basePath + "/oldphone_mono.wav");
        mLinphoneCore.setRootCA(basePath + "/rootca.pem");
        mLinphoneCore.setPlayFile(basePath + "/toy_mono.wav");
        mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");

        int availableCores = Runtime.getRuntime().availableProcessors();
        mLinphoneCore.setCpuCount(availableCores);
    }

    private void setUserAgent() {
        try {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("Myphone", versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private void setFrontCamAsDefault() {
        int camId = 0;
        AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCameraConfiguration.AndroidCamera androidCamera : cameras) {
            if (androidCamera.frontFacing)
                camId = androidCamera.id;
        }
        mLinphoneCore.setVideoDevice(camId);
    }

    private void startIterate() {
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                mLinphoneCore.iterate();
            }
        };

        mTimer = new Timer("my scheduler");
        mTimer.schedule(lTask, 0, 20);
    }



    public static LinphoneCore getLcIfManagerNotDestroyedOrNull() {
        return getLc();
    }

    public static LinphoneCore getLc() {
        return mLinphoneCore;
    }

    @Override
    public void authInfoRequested(LinphoneCore linphoneCore, String s, String s1, String s2) {

    }

    @Override
    public void authenticationRequested(LinphoneCore linphoneCore, LinphoneAuthInfo linphoneAuthInfo, LinphoneCore.AuthMethod authMethod) {

    }


    @Override
    public void callStatsUpdated(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCallStats linphoneCallStats) {

    }

    @Override
    public void newSubscriptionRequest(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend, String s) {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend) {

    }

    @Override
    public void dtmfReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, int i) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneAddress linphoneAddress, byte[] bytes) {

    }

    @Override
    public void transferState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state) {

    }

    @Override
    public void infoReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneInfoMessage linphoneInfoMessage) {

    }

    @Override
    public void subscriptionStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, SubscriptionState subscriptionState) {

    }

    @Override
    public void publishStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, PublishState publishState) {

    }

    @Override
    public void show(LinphoneCore linphoneCore) {

    }

    @Override
    public void displayStatus(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayMessage(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayWarning(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, int i) {

    }

    @Override
    public void fileTransferRecv(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, byte[] bytes, int i) {

    }

    @Override
    public int fileTransferSend(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, ByteBuffer byteBuffer, int i) {
        return 0;
    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {

    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig, LinphoneCore.RegistrationState registrationState, String s) {

    }

    @Override
    public void configuringStatus(LinphoneCore linphoneCore, LinphoneCore.RemoteProvisioningState remoteProvisioningState, String s) {

    }

    @Override
    public void messageReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

    }

    @Override
    public void messageReceivedUnableToDecrypted(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

    }

    public String getRemoteAddress(){
        String address;
        if(mCall == null){
            return null;
        }
        address = mCall.getRemoteAddress().toString();
        return address;
    }


    @Override
    public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String s) {
        if(state == LinphoneCall.State.IncomingReceived){
            Intent intent=new Intent(mContext,Voice.class);
            intent.putExtra("call","no");
            mContext.startActivity(intent);
        }else if (state==LinphoneCall.State.Connected){
            Voice.setIsConected(true);
        }else if (state==LinphoneCall.State.CallReleased){
            Voice.setIsConected(false);
        }else if (state==LinphoneCall.State.CallEnd){
            Intent intent=new Intent(mContext,ContactsList.class);
            mContext.startActivity(intent);
        }
        if (state == LinphoneCall.State.CallUpdatedByRemote){
            // 远方要求改变状态，先固定做改为视频的，改参数
            LinphoneCallParams params = call.getCurrentParamsCopy();
            params.setVideoEnabled(true);
            mLinphoneCore.enableVideo(true, true);

            try {

                mLinphoneCore.acceptCallUpdate(call, params);
                // 开始显示视频

//                Intent intent = new Intent(mContext,VideoTalk.class);
//
//                mContext.startActivity(intent);

                Message msg = new Message();
                msg.what = 1;
                Voice.h.sendMessage(msg);

            } catch (LinphoneCoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public void callEncryptionChanged(LinphoneCore linphoneCore, LinphoneCall linphoneCall, boolean b, String s) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, String s, LinphoneContent linphoneContent) {

    }

    @Override
    public void isComposingReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom) {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore linphoneCore, LinphoneCore.EcCalibratorStatus ecCalibratorStatus, int i, Object o) {

    }

    @Override
    public void uploadProgressIndication(LinphoneCore linphoneCore, int i, int i1) {

    }

    @Override
    public void uploadStateChanged(LinphoneCore linphoneCore, LinphoneCore.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void friendListCreated(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void friendListRemoved(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void networkReachableChanged(LinphoneCore linphoneCore, boolean b) {

    }

}