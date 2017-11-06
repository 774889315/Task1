package com.example.administrator.mymusicplayer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/10/30.
 */

public class MyViewHolder extends RecyclerView.ViewHolder
{
    TextView name;
    Button play, remove;
    public MyViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
        play = (Button) itemView.findViewById(R.id.play);
        remove = (Button) itemView.findViewById(R.id.remove);
    }
}
