package com.example.chenyanzhe.mysipphone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 树宇 on 2017/9/12.
 */

public class ContactsListsDataBaseHelper extends SQLiteOpenHelper {
    public static final String CREAT_CONTACTS="create table if not exists Contacts(" +
            "contactsName text," +
            "sipAdress text primary key)";
    private Context mContext;
    ContactsListsDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context,name,cursorFactory,version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREAT_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
