package com.example.administrator.mymusicplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.administrator.mymusicplayer.R;

import static com.example.administrator.mymusicplayer.activity.MainActivity.myList;

/**
 * Created by Administrator on 2017/11/18.
 */

public class ListActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        myList = (RecyclerView) findViewById(R.id.my_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        myList.setLayoutManager(mLayoutManager);
        myList.setHasFixedSize(true);
        myList.setAdapter(MainActivity.adapter);
    }
}
