package com.blue.bluetoothcomm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class holder extends RecyclerView.ViewHolder {
    public TextView name, address;
    Button pair,mBtn;
    public holder(@NonNull View itemView) {
        super(itemView);
        name=(TextView)itemView.findViewById(R.id.name);
        address=(TextView)itemView.findViewById(R.id.address);
        pair=(Button)itemView.findViewById(R.id.pair);
        mBtn=(Button)itemView.findViewById(R.id.messageBtn);
    }
}
