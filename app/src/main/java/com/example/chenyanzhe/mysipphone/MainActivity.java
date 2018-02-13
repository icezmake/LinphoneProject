package com.example.chenyanzhe.mysipphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    private EditText sip;
    private EditText password;
    private Button login;
    public static MyManager myManager;
    public static ContactsListsDataBaseHelper contactsListsDataBaseHelper;
    private String getsip;
    private String getpassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sip=(EditText)findViewById(R.id.sip);
        contactsListsDataBaseHelper=new ContactsListsDataBaseHelper(this,"Contacts.db",null,1);
        myManager=new MyManager(this);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getsip=sip.getText().toString();
                getpassword=password.getText().toString();
                contactsListsDataBaseHelper.getWritableDatabase();
                myManager.login(getsip,getpassword);
                Intent intent=new Intent(MainActivity.this,ContactsList.class);
                startActivity(intent);
            }
        });
    }

}
