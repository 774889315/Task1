package com.example.administrator.mymusicplayer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.mymusicplayer.R;
import com.example.administrator.mymusicplayer.utils.AllInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2017/11/18.
 */

public class OnlineActivity extends AppCompatActivity
{
    AllInfo info;

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0:
                    Toast.makeText(OnlineActivity.this, "开始下载...", Toast.LENGTH_SHORT).show();
                    break;
                case Integer.MAX_VALUE:
                    Toast.makeText(OnlineActivity.this, "下载成功！", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    Toast.makeText(OnlineActivity.this, "下载失败！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(OnlineActivity.this, "已完成" + msg.what + " kB", Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_activity);
        TextView tv1 = (TextView) findViewById(R.id.name1);
        TextView tv2 = (TextView) findViewById(R.id.name2);
        TextView tv3 = (TextView) findViewById(R.id.name3);

        info = (AllInfo) getIntent().getSerializableExtra("allInfo_data");
        tv1.setText(info.lyric.get(0).getTitle() + " - " + info.lyric.get(0).getArtist());
        tv2.setText(info.lyric.get(1).getTitle() + " - " + info.lyric.get(1).getArtist());
        tv3.setText(info.lyric.get(2).getTitle() + " - " + info.lyric.get(2).getArtist());

        Button bt1 = (Button) findViewById(R.id.play1);
        Button bt2 = (Button) findViewById(R.id.play2);
        Button bt3 = (Button) findViewById(R.id.play3);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(0);
                Log.e("tag0", "clicked");
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(1);
            }
        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(2);
            }
        });
    }

    void download(final int i)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                Log.e("tag0", "download");
                try
                {
                    File fileDir = new File("/storage/emulated/0/Music");
    //                if(!fileDir.exists()) fileDir.mkdir();

                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                    URL url = new URL(info.link.get(i));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    String file = info.lyric.get(i).getTitle() + " - " + info.lyric.get(i).getArtist()+"";
                    InputStream is = conn.getInputStream();
                    OutputStream outs = new FileOutputStream(fileDir + "/" + file + ".mp3");
                    int c;
                    byte buf[] = new byte[1024];
                    int finished = 0;
                    while(true)
                    {
                        int numread = is.read(buf);
                        if (numread <= 0)
                        {
                            break;
                        }
                        outs.write(buf, 0, numread);
                        finished += 1;
                        Log.e("tag0", finished + "kB");
                        msg = new Message();
                        msg.what = finished;
                        if(finished % 500 == 0) handler.sendMessage(msg);
                    }
                    is.close();
                    outs.close();
                    OutputStream outs1 = new FileOutputStream(fileDir + "/" + file + ".lrc");
                    outs1.write(info.lyric.get(i).getLyric().substring(info.lyric.get(i).getLyric().indexOf('[')).getBytes());
                    outs1.close();
                    Log.e("tag0", "finished");
                    msg = new Message();
                    msg.what = Integer.MAX_VALUE;
                    handler.sendMessage(msg);
                }
                catch (Exception e) {
                    Message msg = new Message();
                    msg.what = -1;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
