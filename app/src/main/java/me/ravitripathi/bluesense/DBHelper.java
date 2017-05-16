package me.ravitripathi.bluesense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ravi on 15-05-2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "arcBase.db";
    public static final String DATA_TABLE_NAME = "DATA";
    public static final String DATA_COLUMN_SNO = "SNO";
    public static final String DATA_COLUMN_ASLIP = "ASLIP";
    public static final String DATA_COLUMN_BSLIP = "BSLIP";
    public static final String DATA_COLUMN_CSLIP = "CSLIP";
    public static final String DATA_COLUMN_DSLIP = "DSLIP";
    public static final String DATA_COLUMN_SLIPRAT = "SLIPRAT";


    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table DATA " +
                        "(SNO integer primary key AUTOINCREMENT, ASLIP text,BSLIP text,CSLIP text, DSLIP text,SLIPRAT text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS DATA");
        onCreate(db);
    }

    public boolean insertData (String ASLIP, String BSLIP, String CSLIP, String DSLIP,String SLIPRAT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ASLIP", ASLIP);
        contentValues.put("BSLIP", BSLIP);
        contentValues.put("CSLIP", CSLIP);
        contentValues.put("DSLIP", DSLIP);
        contentValues.put("SLIPRAT", SLIPRAT);
        db.insert("DATA", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from DATA where id="+id+"", null );
        return res;
    }

    public Cursor getAllDataItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from DATA", null );
        return res;
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, DATA_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (int id,String ASLIP, String BSLIP, String CSLIP, String DSLIP,String SLIPRAT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ASLIP", ASLIP);
        contentValues.put("BSLIP", BSLIP);
        contentValues.put("CSLIP", CSLIP);
        contentValues.put("DSLIP", DSLIP);
        contentValues.put("SLIPRAT", SLIPRAT);
        db.update("DATA", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

//    public ArrayList<String> getAllData() {
//        ArrayList<String> array_list = new ArrayList<String>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res =  db.rawQuery( "select * from DATA", null );
//        res.moveToFirst();
//
//        while(res.isAfterLast() == false){
//            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
//            res.moveToNext();
//        }
//        return array_list;
//    }

}