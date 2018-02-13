package com.example.chenyanzhe.mysipphone;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsList extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView contactsListView;
    private Button callDirectVoice;
    private Button addContact;
    private Button callDirectVideo;
    public ContactsListsDataBaseHelper dataBaseHelper;
    public static MyManager mManager;
    private EditText callSipName;
    private ArrayList<Contacts>  contactsArrayList =new ArrayList<Contacts>();
    private ContactsAdapter adapter;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        dataBaseHelper=MainActivity.contactsListsDataBaseHelper;
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        adapter=new ContactsAdapter(this,R.layout.contacts_list,contactsArrayList);
        mManager = MainActivity.myManager;
        callDirectVoice=(Button)findViewById(R.id.call_direct_voice);
        callDirectVideo=(Button)findViewById(R.id.call_direct_video);
        callDirectVideo.setOnClickListener(this);
        callSipName=(EditText)findViewById(R.id.callsipname);
        addContact=(Button)findViewById(R.id.add_contact);
        contactsListView=(ListView)findViewById(R.id.list_view);
        contactsListView.setOnItemClickListener(this);
        addContact.setOnClickListener(this);
        callDirectVoice.setOnClickListener(this);
        contactsListView.setAdapter(adapter);

        Cursor cursor=db.query("Contacts",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String contactsName=cursor.getString(cursor.getColumnIndex("contactsName"));
                String sipAdress=cursor.getString(cursor.getColumnIndex("sipAdress"));
                Contacts contacts=new Contacts(contactsName,sipAdress);
                contactsArrayList.add(contacts);
            }while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
        cursor.close();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.call_direct_voice:
                //直接拨打号码
                if (ContextCompat.checkSelfPermission(ContactsList.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ContactsList.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                }else {
                    String sipName = callSipName.getText().toString();
                    Intent intent = new Intent(ContactsList.this, Voice.class);
                    intent.putExtra("call", "yes");
                    intent.putExtra("contactsName", sipName);
                    startActivity(intent);
                }
                break;
            case R.id.call_direct_video:
                if (ContextCompat.checkSelfPermission(ContactsList.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(ContactsList.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ContactsList.this,new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},2);
                }else {
                String sipName2=callSipName.getText().toString();
                Intent intent2 = new Intent(ContactsList.this,VideoTalk.class);
                intent2.putExtra("call","yes");
                intent2.putExtra("contactsName",sipName2);
                startActivity(intent2);
                }
                break;
            case R.id.add_contact:
                Intent intent1=new Intent(ContactsList.this,AddContacts.class);
                startActivityForResult(intent1,1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, final View view, final int position, long id) {
        dialog=new AlertDialog.Builder(ContactsList.this)
                .setTitle("请选择联系方式")
                .setPositiveButton("语音聊天", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Contacts contacts = contactsArrayList.get(position);
                        Intent intent = new Intent(ContactsList.this, Voice.class);
                        intent.putExtra("call", "yes");
                        intent.putExtra("contactsName", contacts.getContacts());
                        Toast.makeText(ContactsList.this,"lll",Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        //得到点击的联系人信息
                        //跳转语音通话
                    }
                })
                .setNegativeButton("视频聊天", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Contacts contacts = contactsArrayList.get(position);
                        Intent intent = new Intent(ContactsList.this, VideoTalk.class);
                        intent.putExtra("call", "yes");
                        intent.putExtra("contactsName", contacts.getContacts());
                        startActivity(intent);
                        //得到点击的联系人信息
                        //跳转视频通话
                    }
                }) .setNeutralButton("删除",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLiteDatabase db=dataBaseHelper.getWritableDatabase();
                        String contac=contactsArrayList.get(position).getSipAdress();
                        db.delete("Contacts","sipAdress = ?",new String[]{contac});
                        contactsArrayList.remove(position);
                        adapter.notifyDataSetChanged();
                        dialog.cancel();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    String contactName=intent.getStringExtra("contactsName");
                    String sipAdress=intent.getStringExtra("sipAdress");
                    Contacts contacts=new Contacts(contactName,sipAdress);
                    SQLiteDatabase db=dataBaseHelper.getWritableDatabase();
                    ContentValues values=new ContentValues();
                    values.put("contactsName",contactName);
                    values.put("sipAdress",sipAdress);
                    db.insert("Contacts",null,values);
                    values.clear();
                    contactsArrayList.add(contacts);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    String sipName = callSipName.getText().toString();
                    Intent intent = new Intent(ContactsList.this, Voice.class);
                    intent.putExtra("call", "no");
                    intent.putExtra("contactsName", sipName);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ContactsList.this,"未获得权限",Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                int t=0;
                for(int i=0;i<grantResults.length;i++){
                    if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        t=1;
                    }
                }
                if (t==1){
                    Toast.makeText(ContactsList.this,"未获得权限",Toast.LENGTH_SHORT).show();
                }else{
                    String sipName2=callSipName.getText().toString();
                    Intent intent2 = new Intent(ContactsList.this,VideoTalk.class);
                    intent2.putExtra("call","no");
                    intent2.putExtra("contactsName",sipName2);
                    startActivity(intent2);
                }
                break;
            default:
                break;
        }
    }
}


class ContactsAdapter extends ArrayAdapter<Contacts> {
    private int resourseId;
    public static   HashMap<Integer, Integer> visiblecheck ;//用来记录是否显示checkBox
    public static HashMap<Integer, Boolean> ischeck;
    class ViewHolder{
        TextView contactsText;
        TextView sipAdressText;
    }
    public ContactsAdapter(Context context, int textViewResourseId, List<Contacts> objects){
        super(context,textViewResourseId,objects);
        resourseId=textViewResourseId;
        for (int i = 0; i < objects.size(); i++) {
            ischeck.put(i, false);
            visiblecheck.put(i, CheckBox.INVISIBLE);
        }
    }
    @Override
    public View getView(int position, View conventView, ViewGroup parent){
        Contacts contacts1=getItem(position);
        ViewHolder viewHolder;
        View view;
        if (conventView==null){
            view = LayoutInflater.from(getContext()).inflate(resourseId, parent, false);
            viewHolder=new ViewHolder();
            viewHolder.contactsText=(TextView)view.findViewById(R.id.contacts);
            viewHolder.sipAdressText=(TextView)view.findViewById(R.id.sip_adress);
            view.setTag(viewHolder);
        }
        else{
            view=conventView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.sipAdressText.setText(contacts1.getSipAdress());
        viewHolder.contactsText.setText(contacts1.getContacts());
        return view;
    }
}