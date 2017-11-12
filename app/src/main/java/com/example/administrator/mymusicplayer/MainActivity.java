package com.example.administrator.mymusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    static MediaPlayer mp;
    SeekBar sb;
    Button stop, pause, shift, timer;
    TextView tv, tv2, now0, lyric;
    static RecyclerView myList;
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

    int state;
    int now;//现在播放的音乐序号

    Thread myTimer;
    Thread thread;

    static MyAdapter adapter;

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
                            mViewPager.setCurrentItem(now, false);
                            mMusicAdapter.fragment.get(now).anim.start();
                            break;
                        case ORDER:
                            now += 1;
                            if (now == total) now = 0;
                            play(adapter.list.get(now));
                            mViewPager.setCurrentItem(now);
                            mMusicAdapter.fragment.get(now).anim.start();
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
                mViewPager.setCurrentItem(msg.what);

            } else {
                adapter.notifyItemRemoved(-msg.what - 1);
                adapter.list.remove(-msg.what - 1);
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
                lyric.setAnimation(anm);
                anm.start();

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
                }.start();
            }
            else if(musics.get(now).lyric == null) lyric.setText("\n(没有歌词)");
            //         Toast.makeText(MainActivity.this, msg.what + "", Toast.LENGTH_SHORT).show();
        }
    };

    public Handler scrollHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            lyric.scrollTo(0, (int) (msg.what * lyric.getLineHeight()));
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
        lyric = (TextView) findViewById(R.id.lyric);

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

        myList = (RecyclerView) findViewById(R.id.my_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        myList.setLayoutManager(mLayoutManager);
        myList.setHasFixedSize(true);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mViewPager = (ViewPager) findViewById(R.id.container);

        adapter = new MyAdapter(MainActivity.this, musics, playHandler);
        update(adapter);
        //     Toast.makeText(this, adapter.list.size()+"", Toast.LENGTH_SHORT).show();

        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                reader.setVisibility(View.INVISIBLE);
            }
        });

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
                    Message msg = new Message();
                    msg.what = position;
                    playHandler.sendMessage(msg);
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

                if(state == 2 && mp.isPlaying()) //滑动完
                {
                    for(int i = 0; i < mMusicAdapter.fragment.size(); i++)
                        mMusicAdapter.fragment.get(i).anim.start();
                }
            }
        });

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
                lyric.setText("\n");
                for(int i = 0; i < musics.get(now).lyric.size(); i++)
                {
                    lyric.append(musics.get(now).lyric.get(i).getLyric()+"\n");
                }

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
            return musics.size();
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


            if(musics.size() > getArguments().getInt(ARG_SECTION_NUMBER))
            {
                if(musics.get(getArguments().getInt(ARG_SECTION_NUMBER)) == null) name.setText("null");
                else name.setText(musics.get(getArguments().getInt(ARG_SECTION_NUMBER)).getName());
            }
            return rootView;
        }
    }
}












