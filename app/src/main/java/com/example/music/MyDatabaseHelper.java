package com.example.music;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static  final  String create_word ="create table Music ("
            +"Name String primary key ,"
            +"Path String )";

    private  Context mContext;

    public MyDatabaseHelper( Context context,  String name,  SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(create_word);
        Toast.makeText(mContext,"Create success",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}