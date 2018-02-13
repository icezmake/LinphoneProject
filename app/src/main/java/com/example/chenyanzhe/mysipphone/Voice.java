package com.example.chenyanzhe.mysipphone;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenyanzhe.mysipphone.R;

import org.linphone.core.LinphoneCoreException;
import org.w3c.dom.Text;

public class Voice extends AppCompatActivity implements View.OnClickListener{
    private TextView contacts_name;
    private Button drop;
    private Button accept;
    private MyManager mManager;
    private TextView mContact;
    private static boolean isConected;
    private static boolean isInvite;
    public static Handler h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        isConected = false;
        drop = (Button) findViewById(R.id.drop_call);
        accept = (Button) findViewById(R.id.accept_call);
        mContact = (TextView) findViewById(R.id.contacts_name);
        drop.setOnClickListener(this);
        mManager = ContactsList.mManager;
        accept.setOnClickListener(this);
        contacts_name = (TextView) findViewById(R.id.contacts_name);
        Intent intent = getIntent();
        String name = intent.getStringExtra("contactsName");
        contacts_name.setText(name);
        if ("yes".equals(intent.getStringExtra("call"))) {
            accept.setVisibility(View.INVISIBLE);
            String des = "sip:" + name + "@sip.linphone.org";
            isInvite = true;
            mManager.invite(des);
            mContact.setText("Calling to "+name);
        } else {
            accept.setVisibility(View.VISIBLE);
            accept.setEnabled(true);
            isInvite = false;
            mContact.setText(mManager.getRemoteAddress());
        }

        h = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(Voice.this, "UpdateCall", Toast.LENGTH_SHORT).show();
                if (msg.what == 1) {
                    VideoCallFragment videoCallFragment = new VideoCallFragment();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.activity_voice, videoCallFragment);
                    try {
                        transaction.commitAllowingStateLoss();
                    } catch (Exception e) {

                    }
//                    Intent intent = new Intent(ContactsList.this,VideoTalk.class);
//
//                    startActivity(intent);
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.drop_call:
                //实现挂断电话
                if (isConected || isInvite){
                    mManager.hangUp();//挂断以及拒接
                }else{
                    mManager.declineCall();//主动终止当前未连接通话
                }
                finish();
                break;
            case R.id.accept_call:
                //实现接听电话
                try {

                    mManager.acceptCall();
                } catch (LinphoneCoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                accept.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

   public static void setIsConected(boolean state){
       isConected=state;
   }

}
