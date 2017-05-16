package me.ravitripathi.bluesense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ravi on 15-05-2017.
 */


public class CustomListAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> objects;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public CustomListAdapter(Context context, int textViewResourceId, ArrayList<Item> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

		/*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Item i = objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView sno = (TextView) v.findViewById(R.id.sno);
            TextView aslip = (TextView) v.findViewById(R.id.ASLIP);
            TextView bslip = (TextView) v.findViewById(R.id.BSLIP);
            TextView cslip = (TextView) v.findViewById(R.id.CSLIP);
            TextView dslip = (TextView) v.findViewById(R.id.DSLIP);
            TextView sliprat = (TextView) v.findViewById(R.id.SLIPRATE);

            sno.setText("S.No."+i.getSNO());
            aslip.setText("Slip of wheel A: "+i.getASLIP());
            bslip.setText("Slip of wheel B: "+i.getBSLIP());
            cslip.setText("Slip of wheel C: "+i.getCSLIP());
            dslip.setText("Slip of wheel D: "+i.getDSLIP());
            sliprat.setText("Slip Ratio: "+i.getSLIPRAT());
//            TextView head = (TextView) v.findViewById(R.id.head);
//            TextView details = (TextView) v.findViewById(R.id.det);
//
//            if (head != null)
//                head.setText(i.getHeading());
//
//            if (details != null)
//                details.setText(i.getData());
        }

        return v;
    }
}
