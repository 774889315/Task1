package com.example.administrator.mymusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    MediaPlayer mp;
    SeekBar sb;
    Button stop, pause, shift, timer;
    TextView tv, tv2, now0;
    static RecyclerView myList;
    ArrayList<Music> test;

    NotificationManager manager;
    RemoteViews remoteViews;

    final static int ONCE = 1;
    final static int LOOP = 2;
    final static int ORDER = 3;
    final static int RANDOM = 4;

    final static int MAX = 10000;

    final static String ACTION_PAUSE = "com.example.action.ButtonClick";
    final static int RANDOM_CODE = (int) (Math.random() * 65536);

    int state;
    int now;//现在播放的音乐序号

    Thread myTimer;

    static MyAdapter adapter;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            update();
            int what;
            switch (what = msg.what) {
                case MAX << 1:
                    myList.setAdapter(adapter);
                    break;
                case MAX:
                    sb.setProgress(0);
                    tv.setText("00:00/00:00");
                    mp.seekTo(0);
                    int total = adapter.list.size();
                    switch (state) {
                        case ONCE:
                            pause.setText("开始");
                            break;
                        case LOOP:
                            mp.start();
                            break;
                        case RANDOM:
                            now = (int) (Math.random() * total);
                            play(adapter.list.get(now));
                            break;
                        case ORDER:
                            now += 1;
                            if (now == total) now = 0;
                            play(adapter.list.get(now));
                            break;
                    }
                    break;
                case -1:
                    mp.pause();
                    sb.setProgress(0);
                    tv.setText("00:00/00:00");
                    mp.seekTo(0);
                    pause.setText("开始");
                default:
                    if (what >= 0) sb.setProgress(what);
                    else {
                        int time = -what - 1;
                        tv2.setText("定时: " + time / 60 + ":" + time % 60);
                    }
            }
        }
    };

    public Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what >= 0) {
                now = msg.what;
                mp.stop();
                sb.setProgress(0);
                tv.setText("00:00/00:00");
                pause.setText("暂停");
                play(adapter.list.get(msg.what));
            } else {
                adapter.notifyItemRemoved(-msg.what - 1);
                adapter.list.remove(-msg.what - 1);
            }
        }
    };

    MyBroadcastReceiver receiver = new MyBroadcastReceiver();

    @Override
    public void onClick(View v) {
        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener()
        {

            @Override
            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
                myTimer = new Thread()
                {
                    int time = - hourOfDay * 3600 - minute * 60 - 1;
                    @Override
                    public void run()
                    {
                        while(time < 0)
                        {
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            time++;
                            Message msg = new Message();
                            msg.what = time;
                            handler.sendMessage(msg);
                        }
                    }
                };
                myTimer.start();
            }
        }, 0, 0, true).show();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        public MyBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "......", Toast.LENGTH_SHORT).show();
            if (mp.isPlaying()) {
                mp.pause();
                pause.setText("继续");
            } else {
                mp.start();
                pause.setText("暂停");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = 1;

        final int REQUEST_EXTERNAL_STORAGE = 1;
        final String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int permission;
        while ((permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }


        mp = new MediaPlayer();

        sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setMax(MAX);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo((int) ((seekBar.getProgress() * (mp.getDuration() + 0L)) / seekBar.getMax()));
            }
        });

        tv = (TextView) findViewById(R.id.time);
        tv2 = (TextView) findViewById(R.id.timer_text);
        now0 = (TextView) findViewById(R.id.now);

        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
                sb.setProgress(0);
                tv.setText("00:00/00:00");
                mp.seekTo(0);
                pause.setText("开始");
                now0.setText("无播放");
                mp = new MediaPlayer();
            }
        });

        pause = (Button) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    pause.setText("继续");
                } else {
                    mp.start();
                    pause.setText("暂停");
                }
            }
        });

        shift = (Button) findViewById(R.id.shift);
        shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state) {
                    case ONCE:
                        state = LOOP;
                        shift.setText("单曲循环");
                        break;
                    case LOOP:
                        state = ORDER;
                        shift.setText("顺序播放");
                        break;
                    case ORDER:
                        state = RANDOM;
                        shift.setText("随机播放");
                        break;
                    case RANDOM:
                        state = ONCE;
                        shift.setText("单曲播放");
                        break;
                }
            }
        });

        timer = (Button) findViewById(R.id.timer);
        timer.setOnClickListener(this);

        myList = (RecyclerView) findViewById(R.id.my_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        myList.setLayoutManager(mLayoutManager);
        myList.setHasFixedSize(true);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        test = new ArrayList<>();
        test.add(new Music("/storage/emulated/0/test/classicriver.mp3"));
        test.add(new Music("/storage/emulated/0/test/迎宾曲 感动中国 交响乐.mp3"));
        test.add(new Music("/storage/emulated/0/test/snowdreams.wav"));

        adapter = new MyAdapter(MainActivity.this, test, playHandler);
        update(adapter);
        //     Toast.makeText(this, adapter.list.size()+"", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int progress = (int) (mp.getCurrentPosition() * (long) MAX / mp.getDuration());
                    //               Log.e("eeeeeeeeeee", mp.getCurrentPosition()+"\t"+mp.getDuration());
                    if (mp.getCurrentPosition() + 200 > mp.getDuration()) progress = MAX;
                    Message msg = new Message();
                    msg.what = progress;
                    handler.sendMessage(msg);
                }

            }
        }.start();
    }


    void update() {
        int c;
        tv.setText((c = mp.getCurrentPosition() / 1000) / 60 + ":" + (c % 60 < 10 ? "0" : "") + c % 60
                + "/" + (c = mp.getDuration() / 1000) / 60 + ":" + (c % 60 < 10 ? "0" : "") + c % 60);
    }

    void update(MyAdapter adapter) {
        myList.setAdapter(adapter);
    }

    void play(Music music) {
        try {
            mp = new MediaPlayer();
            mp.setDataSource(music.getLocation());
            mp.prepare();
            mp.start();
            now0.setText("正在播放：" + music.getName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class MusicAdapter extends FragmentPagerAdapter
    {
        public MusicAdapter(FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}












