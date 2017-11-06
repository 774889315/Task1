package com.example.administrator.mymusicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder>
{

    List<Music> list = new ArrayList<>();
    Handler handler;

    Context context;

    MyAdapter(Context context)
    {
        this.context = context;
    }

    MyAdapter(Context context, ArrayList<Music> list, Handler handler)
    {
        this.context = context;
        this.list = list;
        this.handler = handler;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.my_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position)
    {
        holder.name.setText(list.get(position).getName());
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Message msg = new Message();
                msg.what = -position-1;
                handler.sendMessage(msg);
            }
        });

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = position;
                handler.sendMessage(msg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

