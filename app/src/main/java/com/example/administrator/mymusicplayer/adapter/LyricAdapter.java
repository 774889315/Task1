package com.example.administrator.mymusicplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.mymusicplayer.R;
import com.example.administrator.mymusicplayer.activity.MainActivity;
import com.example.administrator.mymusicplayer.utils.Lyric;

import java.util.List;

/**
 * Created by Administrator on 2017/11/14.
 */

public class LyricAdapter extends RecyclerView.Adapter
{
    List<Lyric> lyric;
    Context context;

    public LyricAdapter(Context context, List<Lyric> lyric)
    {
        this.lyric = lyric;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View lyricView = LayoutInflater.from(context).inflate(R.layout.lyric_item, parent, false);
        lyricView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = lyric.get((int) v.getTag()).getTimeMillis();
                MainActivity.mp.seekTo(time);
            }
        });
        return new LyricViewHolder(lyricView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView lyricText = (TextView) holder.itemView.findViewById(R.id.textView);
        holder.itemView.setTag(position);
   //     Log.e("jjjjjjjj", position+"");

 //       lyricText.setTextColor(Color.rgb(100, 0, 0));


        lyricText.setText(lyric.get(position).getLyric());
    }

    @Override
    public int getItemCount() {
        if(lyric == null) return -1;
        return lyric.size();
    }
}

class LyricViewHolder extends RecyclerView.ViewHolder
{
    public LyricViewHolder(View itemView)
    {
        super(itemView);
 //       Log.e("iiiiiiii", "");
    }
}