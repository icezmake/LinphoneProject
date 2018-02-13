package com.example.chenyanzhe.mysipphone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenyanzhe on 2017/9/1.
 */
public class MyUtils {

    public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(context, ressourceId, lFileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = context.openFileOutput (target, 0);
        InputStream lInputStream = context.getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while (( readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff,0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public static boolean isCallRunning(LinphoneCall call)
    {
        if (call == null) {
            return false;
        }

        LinphoneCall.State state = call.getState();

        return state == LinphoneCall.State.Connected ||
                state == LinphoneCall.State.CallUpdating ||
                state == LinphoneCall.State.CallUpdatedByRemote ||
                state == LinphoneCall.State.StreamsRunning ||
                state == LinphoneCall.State.Resuming;
    }

    public static boolean isCallEstablished(LinphoneCall call) {
        if (call == null) {
            return false;
        }

        LinphoneCall.State state = call.getState();

        return isCallRunning(call) ||
                state == LinphoneCall.State.Paused ||
                state == LinphoneCall.State.PausedByRemote ||
                state == LinphoneCall.State.Pausing;
    }

    public static boolean isHighBandwidthConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(),info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType){
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        //in doubt, assume connection is good.
        return true;
    }

    public static final List<LinphoneCall> getLinphoneCalls(LinphoneCore lc) {
        // return a modifiable list
        return new ArrayList<LinphoneCall>(Arrays.asList(lc.getCalls()));
    }
}
