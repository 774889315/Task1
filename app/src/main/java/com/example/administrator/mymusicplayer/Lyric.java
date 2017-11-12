package com.example.administrator.mymusicplayer;

import android.util.Log;

/**
 * Created by Administrator on 2017/11/8.
 */

public class Lyric
{
    private String lyric;

    Lyric(String s)
    {
        lyric = s;
    }

    int getTimeMillis()
    {
        double time = 0;

one:    for(int i = 0; i < lyric.length(); i++)
        {
            switch(lyric.charAt(i))
            {
                case '[':
                    break;
                case ']':
                    time *= 10;
                    break one;
                case ':':
                    time *= .6;
                    break;
                case '.':
          //          time *= 100;
                    break;
                default:
                    time = time * 10 + lyric.charAt(i) - '0';
            }

        }
   //     Log.e("time", time+"");
        return (int) time;
    }

    String getLyric()
    {
        return lyric.substring(lyric.indexOf(']') + 1);
    }
}
