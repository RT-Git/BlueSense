package me.ravitripathi.bluesense;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    private DBHelper myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        ArrayList<Item> list= new ArrayList<>();
        CustomListAdapter customListAdapter = new CustomListAdapter(this,R.layout.list_item,list);
        ListView listView = (ListView) findViewById(R.id.lV);
        listView.setAdapter(customListAdapter);

        int i=0;
        Item e;
        myDB = new DBHelper(this);
        Cursor cursor = myDB.getAllDataItems();

        if (cursor.moveToFirst()){
            do{
//                "(SNO integer primary key AUTOINCREMENT, ASLIP text,BSLIP text,CSLIP text, DSLIP text,SLIPRAT text)"
                    String sno = String.valueOf(i);
                String aslip = cursor.getString(cursor.getColumnIndex("ASLIP"));
                String bslip = cursor.getString(cursor.getColumnIndex("BSLIP"));
                String cslip = cursor.getString(cursor.getColumnIndex("CSLIP"));
                String dslip = cursor.getString(cursor.getColumnIndex("DSLIP"));
                String slipRAT = cursor.getString(cursor.getColumnIndex("SLIPRAT"));
                e = new Item(sno,aslip,bslip,cslip,dslip,slipRAT);
                list.add(e);
                customListAdapter.notifyDataSetChanged();
                i++;
                // do what ever you want here
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
}
