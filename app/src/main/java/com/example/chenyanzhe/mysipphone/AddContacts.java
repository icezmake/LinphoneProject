package com.example.chenyanzhe.mysipphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddContacts extends AppCompatActivity {

    private Button addContacts;
    private EditText contactName;
    private EditText sipAdress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        addContacts=(Button)findViewById(R.id.add_contact_another);
        contactName=(EditText)findViewById(R.id.add_contact_name);
        sipAdress=(EditText)findViewById(R.id.add_sip_adress);
        addContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactname=contactName.getText().toString();
                String sipadress=sipAdress.getText().toString();
                Intent intent=new Intent();
                intent.putExtra("contactsName",contactname);
                intent.putExtra("sipAdress",sipadress);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
}
