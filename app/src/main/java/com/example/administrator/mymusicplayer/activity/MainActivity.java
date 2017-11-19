package com.example.administrator.mymusicplayer.activity;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.administrator.mymusicplayer.adapter.LyricAdapter;
//import com.example.administrator.mymusicplayer.utils.StringEscapeUtils;
import com.example.administrator.mymusicplayer.fragment.MyFragment;
import com.example.administrator.mymusicplayer.utils.AllInfo;
import com.example.administrator.mymusicplayer.utils.Lyric;
import com.example.administrator.mymusicplayer.utils.Staff;
import com.example.administrator.mymusicplayer.widget.AppWidget;
import com.example.administrator.mymusicplayer.utils.Music;
import com.example.administrator.mymusicplayer.adapter.MyAdapter;
import com.example.administrator.mymusicplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.google.gson.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static MediaPlayer mp;
    SeekBar sb;
    Button stop, pause, shift, timer, get, showList;
    TextView tv, tv2, now0/*, lyric*/;
    static RecyclerView myList;
    RecyclerView myLyric;
    static ArrayList<Music> musics;

    static int previous = 0;

    NotificationManager manager;
    RemoteViews remoteViews;

    final static int ONCE = 1;
    final static int LOOP = 2;
    final static int ORDER = 3;
    final static int RANDOM = 4;

    final static int MAX = 10000;

    final static String ACTION_PAUSE = "com.example.action.ButtonClick";
    final static int RANDOM_CODE = (int) (Math.random() * 65536);

    static boolean stopped;
    boolean showLyric = false;

    int state;
    int now;//现在播放的音乐序号

    Thread myTimer;
    Thread thread;

    static MyAdapter adapter;
    LyricAdapter lyricAdapter;

    private MusicAdapter mMusicAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    ImageView reader;
    AnimationDrawable anim;
    Animation animation, animation1;

    RelativeLayout relativeLayout;
    LinearLayout tape;

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
                            mViewPager.setCurrentItem(now+1, false);
                            mMusicAdapter.fragment.get(now+1).anim.start();
                            break;
                        case ORDER:
                            now += 1;
                            if (now == total) now = 0;
                            play(adapter.list.get(now));
                            mViewPager.setCurrentItem(now+1, false);
                            mMusicAdapter.fragment.get(now+1).anim.start();
                            break;
                    }
                    break;
                case -1:
                    mp.pause();
                    sb.setProgress(0);
                    tv.setText("00:00/00:00");
                    mp.seekTo(0);
                    pause.setText("开始");
                    mMusicAdapter.fragment.get(now).anim.stop();
                    break;
                case Integer.MIN_VALUE:
                    Log.e("tag0", "clicked0");
                    relativeLayout.setVisibility(View.INVISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                    reader.setVisibility(View.VISIBLE);
                    break;
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
                mViewPager.setCurrentItem(msg.what+1, false);

            } else {
                adapter.notifyItemRemoved(-msg.what - 1);
                new File(adapter.list.get(-msg.what - 1).getLocation()).delete();
                adapter.list.remove(-msg.what - 1);
                musics.remove(-msg.what - 1);
            }
        }
    };

    public Handler lyricHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if(musics.get(now).lyric != null && previous != msg.what)
            {
                previous = msg.what;
                Animation anm = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scroll_anim);
       //         lyric.setAnimation(anm);
                myLyric.setAnimation(anm);
                anm.start();

      //          myLyric.smoothScrollToPosition(msg.what + 1);
                myLyric.scrollToPosition(msg.what + 1);

                for(int i = 0; i < musics.get(now).lyric.size(); i++)
                {
                    if(myLyric.findViewHolderForAdapterPosition(i) == null) continue;
   //                 Log.e("tag000000", myLyric.findViewHolderForAdapterPosition(i)+"");
                    TextView tv = (TextView) (myLyric.findViewHolderForLayoutPosition(i).itemView.findViewById(R.id.textView));
                    tv.setTextSize(18);
                    tv.setTextColor(Color.rgb(50, 50, 50));
                }

                if(myLyric.findViewHolderForAdapterPosition(msg.what) == null) return;
                TextView tv = (TextView) (myLyric.findViewHolderForLayoutPosition(msg.what).itemView.findViewById(R.id.textView));
                tv.setTextSize(24);
                tv.setTextColor(Color.rgb(100, 0, 200));

           //     myLyric.
/*
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message msg0 = new Message();
                        msg0.what = previous;
                        scrollHandler.sendMessage(msg0);

                    }
                }.start();*/
            }
     //       else if(musics.get(now).lyric == null) lyric.setText("\n(没有歌词)");
            //         Toast.makeText(MainActivity.this, msg.what + "", Toast.LENGTH_SHORT).show();
        }
    };

    public Handler scrollHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
 //           lyric.scrollTo(0, (int) (msg.what * lyric.getLineHeight()));
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

    public static class MyBroadcastReceiver extends BroadcastReceiver {
        public MyBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("action_pause") && mp.isPlaying()) {
                Toast.makeText(context, "......", Toast.LENGTH_SHORT).show();
                {
                    mp.pause();
                    //           pause.setText("继续");
                    //           mMusicAdapter.fragment.get(now).anim.start();
                }
            }

            if (action.equals("action_play") && !mp.isPlaying()) {
                    mp.start();
         //           pause.setText("暂停");
         //           mMusicAdapter.fragment.get(now).anim.stop();

            }

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);

            //       remoteViews.setTextViewText(R.id.play, ""+System.currentTimeMillis());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName componentName = new ComponentName(context, MainActivity.class);

            // 更新appWidget
            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }
    }

    AppWidget test = new AppWidget();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musics = new ArrayList<>();

        File fileDir = new File("/storage/emulated/0/Music/");
   //     if(!fileDir.exists()) fileDir.mkdir();

        File file[] = fileDir.listFiles();

        for (int i = 0; i < file.length; i++) {
            if(!file[i].getName().substring(file[i].getName().lastIndexOf('.')).equals(".lrc") && file[i].getName().charAt(0) != '.')
                musics.add(new Music(file[i].toString()));
        }

        musics.add(new Music("/storage/emulated/0/test/classicriver.mp3"));
        musics.add(new Music("/storage/emulated/0/test.mp3"));
        musics.add(new Music("/storage/emulated/0/test/snowdreams.wav"));

        reader = (ImageView) findViewById(R.id.reader);



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
  //      lyric = (TextView) findViewById(R.id.lyric);

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
                animation1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reader_anim1);
                reader.setAnimation(animation1);
                animation1.start();
                for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                    mMusicAdapter.fragment.get(i).anim.stop();
            }
        });

        pause = (Button) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true)                   //???
                {
                    if (mp.isPlaying()) {
                        mp.pause();
                        pause.setText("继续");
                        for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                            mMusicAdapter.fragment.get(i).anim.stop();
                    } else {
                        mp.start();
                        pause.setText("暂停");
                        for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                            mMusicAdapter.fragment.get(i).anim.start();
                    }
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

        get = (Button) findViewById(R.id.get);
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            URL url = new URL("http://tingapi.ting.baidu.com/v1/restserver/ting?" +
                                    "method=baidu.ting.billboard.billList&type=22&size=3&offset=0");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.connect();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            StringBuffer sb = new StringBuffer();
                            String temp;
                            while((temp = reader.readLine()) != null) sb.append(temp);
                            reader.close();
//                            Log.e("tag0", sb.toString()+"\n");
                            int id[] = getSongId(sb.toString());
                   //         Log.e("tag0", id[0]+"\t"+id[1]+"\t"+id[2]);

           //                 Lyric[] lyric = new Lyric[id.length];

                            AllInfo info = new AllInfo();


                            for(int i = 0; i < id.length; i++)
                            {
                                url = new URL("http://tingapi.ting.baidu.com/v1/restserver/ting?" +
                                        "method=baidu.ting.song.lry&songid=" + id[i]);
                                conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("GET");
                                conn.connect();
                                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));



//                                sb = new StringBuffer();
//                                while((temp = reader.readLine()) != null) sb.append(temp);

      //                          String lyric = getLyric(sb.toString());
                   //             Log.e("tag0", sb+"");

                                Gson gson = new Gson();
                                Staff staff = gson.fromJson(reader, Staff.class);
               //                 Log.e("tag0", staff.getTitle()+"\t"+staff.getLrcContent());
           //                     lyric[i] = new Lyric(staff.getLrcContent(), true);
                                info.lyric.add(new Lyric(staff.getLrcContent(), true));
                                reader.close();



                                url = new URL("http://ting.baidu.com/data/music/links?songIds={"+id[i]+"}");
                                conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("GET");
                                conn.connect();
                                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));


//
//                                          sb = new StringBuffer();
//                                           while((temp = reader.readLine()) != null) sb.append(temp);
//
//                                                      Log.e("tag0", sb+"");

                    //            gson = new Gson();
                                staff = gson.fromJson(reader, Staff.class);
    //                            Log.e("tag0", staff.getData().getSongList().get(0).getSongLink());
                                info.link.add(staff.getData().getSongList().get(0).getSongLink());
                                reader.close();


                            }

                            Intent intent = new Intent(MainActivity.this, OnlineActivity.class);
                            intent.putExtra("allInfo_data", info);
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        showList = (Button) findViewById(R.id.show_list);
        showList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*MyFragment fragment = new MyFragment();
                android.app.FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.first_fragment, fragment);
                transaction.commit();*/

                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });



        myLyric = (RecyclerView) findViewById(R.id.my_lyric);
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(this);
        myLyric.setLayoutManager(mLayoutManager1);
        myLyric.setHasFixedSize(true);


        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mViewPager = (ViewPager) findViewById(R.id.container);

        adapter = new MyAdapter(MainActivity.this, musics, playHandler);
        update(adapter);
        //     Toast.makeText(this, adapter.list.size()+"", Toast.LENGTH_SHORT).show();

        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);

        class TapGestureListener extends GestureDetector.SimpleOnGestureListener{

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                relativeLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                reader.setVisibility(View.INVISIBLE);
                return false;
            }
        }
        final GestureDetector tapGestureDetector = new GestureDetector(this, new TapGestureListener());

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        class TapGestureListener1 extends GestureDetector.SimpleOnGestureListener{

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                if(!showLyric)
                {
                    relativeLayout.setVisibility(View.INVISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                    reader.setVisibility(View.VISIBLE);
                    Log.e("tag0", "clicked");
                }
                /*else
                {
                    relativeLayout.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.INVISIBLE);
                    reader.setVisibility(View.INVISIBLE);
                    return false;
                }
                showLyric = !showLyric;*/
                return false;
            }
        }
        final GestureDetector tapGestureDetector1 = new GestureDetector(this, new TapGestureListener1());

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout0);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = Integer.MIN_VALUE;
                handler.sendMessage(msg);
            }
        });

        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                reader.setVisibility(View.INVISIBLE);
            }
        });

        mViewPager.setCurrentItem(1);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //            PlaceholderFragment.animation1.start();
                if(mp.isPlaying())
                {
                    animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reader_anim);
                    reader.setAnimation(animation);
                    animation.start();
                }
            }

            @Override
            public void onPageSelected(int position) {

                if(mp.isPlaying())
                {
//                    final Message msg = new Message();
//                    msg.what = position;
//                    playHandler.sendMessage(msg);

                    //                animation1.start();
                    //                PlaceholderFragment.animation.start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //             animation.start();
                if(state == 1 && mp.isPlaying()) //正在滑动
                {
                    animation1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reader_anim1);
                    reader.setAnimation(animation1);
                    animation1.start();
                    for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                        mMusicAdapter.fragment.get(i).anim.stop();
                }

                if(state == 0) //滑动完
                {
  //                  Log.e("tag0", mViewPager.getCurrentItem()+"");
                    if(mViewPager.getCurrentItem() == 0) mViewPager.setCurrentItem(adapter.list.size(), false);
                    else if(mViewPager.getCurrentItem() == adapter.list.size() + 1) mViewPager.setCurrentItem(1, false);

                    if(mp.isPlaying())
                    {
                        final Message msg = new Message();
                        msg.what = mViewPager.getCurrentItem() - 1;
                        playHandler.sendMessage(msg);
                        for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                            mMusicAdapter.fragment.get(i).anim.start();
                    }
                }
            }
        });
/*
        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
  //      tape = (LinearLayout) findViewById(R.id.tape);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click", "AAAAAAAAAAAAAAAAAAAAAAAA");
                relativeLayout.setVisibility(View.INVISIBLE);
                mViewPager.setVisibility(View.VISIBLE);
                reader.setVisibility(View.VISIBLE);
            }
        });

*/

    }

    int[] getSongId(String s)
    {
        int loc = s.indexOf("song_id");
        while(s.charAt(loc) != ':') loc += 1;
        int number = 0;
        for(loc += 2; s.charAt(loc) != '\"'; loc++)
        {
            number *= 10;
            number += s.charAt(loc) - '0';
        }

        loc = s.indexOf("song_id", loc);
        while(s.charAt(loc) != ':') loc += 1;
        int number1 = 0;
        for(loc += 2; s.charAt(loc) != '\"'; loc++)
        {
            number1 *= 10;
            number1 += s.charAt(loc) - '0';
        }

        loc = s.indexOf("song_id", loc);
        while(s.charAt(loc) != ':') loc += 1;
        int number2 = 0;
        for(loc += 2; s.charAt(loc) != '\"'; loc++)
        {
            number2 *= 10;
            number2 += s.charAt(loc) - '0';
        }
        return new int[]{number, number1, number2};
    }

    String getLyric(String s)
    {
        StringBuilder sb = new StringBuilder();
        int c;
        int i = s.indexOf("lrcContent");
        while((c = s.charAt(i)) != ':') i++;
        i += 2;
        while((c = s.charAt(i)) != '\"')
        {
            sb.append((char)c);
            i += 1;
        }
//        return String.valueOf(sb);
        return sb.toString();
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

    @Override
    protected void onResume()
    {
        super.onResume();
        /*musics = new ArrayList<>();

        File fileDir = new File("/storage/emulated/0/Music/");
        //     if(!fileDir.exists()) fileDir.mkdir();

        File file[] = fileDir.listFiles();

        for(int i = 0; i < file.length; i++)
        {
            if(!file[i].getName().substring(file[i].getName().lastIndexOf('.')).equals(".lrc") && file[i].getName().charAt(0) != '.')
                musics.add(new Music(file[i].getName()));
        }

        musics.add(new Music("/storage/emulated/0/test/classicriver.mp3"));
        musics.add(new Music("/storage/emulated/0/test.mp3"));
        musics.add(new Music("/storage/emulated/0/test/snowdreams.wav"));
        update(adapter);*/
    }


    void update() {
        int c;
        tv.setText((c = mp.getCurrentPosition() / 1000) / 60 + ":" + (c % 60 < 10 ? "0" : "") + c % 60
                + "/" + (c = mp.getDuration() / 1000) / 60 + ":" + (c % 60 < 10 ? "0" : "") + c % 60);
    }

    void update(MyAdapter adapter) {
  //      myList.setAdapter(adapter);

        mMusicAdapter = new MusicAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mMusicAdapter);
        mViewPager.setCurrentItem(0);
    }

    void play(final Music music) {
        try {
            stopped = true;
            mp = new MediaPlayer();
            mp.setDataSource(music.getLocation());
            mp.prepare();
            mp.start();
            now0.setText("正在播放：" + music.getName());
            animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reader_anim);
            reader.setAnimation(animation);
            animation.start();
            for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                mMusicAdapter.fragment.get(i).anim.start();

            if(music.lyric != null)
            {
                /*
                lyric.setText("\n");
                for(int i = 0; i < musics.get(now).lyric.size(); i++)
                {
                    lyric.append(musics.get(now).lyric.get(i).getLyric()+"\n");
                }
*/
                myLyric = (RecyclerView) findViewById(R.id.my_lyric);
                LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(this);
                myLyric.setLayoutManager(mLayoutManager1);
                myLyric.setHasFixedSize(true);

                lyricAdapter = new LyricAdapter(MainActivity.this, music.lyric);
                myLyric.setAdapter(lyricAdapter);

                thread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopped = false;
                        for(;;)
                        {
                            try {
                                if(!stopped) sleep(20);
                                else break;
                                int timeNow = mp.getCurrentPosition();
                                int i;
                                for(i = 0; i < music.lyric.size(); i++)
                                {
         //                           Log.e("time", timeNow+"\t"+music.lyric.get(i + 1).getTimeMillis());
                                    if(i == music.lyric.size() - 1 || music.lyric.get(i + 1).getTimeMillis() > timeNow)
                                        break;
                                }
                                Message msg = new Message();
                                msg.what = i;
                                lyricHandler.sendMessage(msg);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class MusicAdapter extends FragmentPagerAdapter
    {
        ArrayList<PlaceholderFragment> fragment;
        public MusicAdapter(FragmentManager fm)
        {
            super(fm);
            fragment = new ArrayList<>();
        }

        @Override
        public PlaceholderFragment getItem(int position) {
            PlaceholderFragment fragment = PlaceholderFragment.newInstance(position);
            this.fragment.add(fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return musics.size() + 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment
    {
        private static final String ARG_SECTION_NUMBER = "section_number";
        AnimationDrawable anim;

        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView name = (TextView) rootView.findViewById(R.id.name);
            ImageView tape = (ImageView) rootView.findViewById(R.id.tape);
            anim = (AnimationDrawable) tape.getDrawable();


//            if(musics.size() > getArguments().getInt(ARG_SECTION_NUMBER))
            {
//                if(musics.get(getArguments().getInt(ARG_SECTION_NUMBER)) == null) name.setText("null");
//                else
                {
                    if(getArguments().getInt(ARG_SECTION_NUMBER) == 0) name.setText(musics.get(musics.size()-1).getName());
                    else if(getArguments().getInt(ARG_SECTION_NUMBER) == musics.size()+1) name.setText(musics.get(0).getName());
                    else name.setText(musics.get(getArguments().getInt(ARG_SECTION_NUMBER)- 1).getName());
                }
            }
            return rootView;
        }
    }
}












