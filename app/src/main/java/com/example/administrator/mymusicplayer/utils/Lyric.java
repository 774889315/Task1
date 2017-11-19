package com.example.administrator.mymusicplayer.utils;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/8.
 */

public class Lyric implements Serializable
{
    private String lyric, title, artist, album;

    private boolean hasHead;

    Lyric(String s)
    {
        lyric = s;
    }

    public Lyric(String s, boolean hasHead)
    {
        if(!hasHead) lyric = s;
        else
        {
            int i = s.indexOf("[ti");
            while(s.charAt(i) != ':') i++;
            i += 1;
            StringBuilder sb = new StringBuilder();
            for(; s.charAt(i) != ']'; i++)
            {
                sb.append(s.charAt(i));
            }
            title = sb.toString();

            i = s.indexOf("[ar");
            while(s.charAt(i - 1) != ':') i++;
            sb = new StringBuilder();
            for(; s.charAt(i) != ']'; i++)
            {
                sb.append(s.charAt(i));
            }
            artist = sb.toString();

            i = s.indexOf("[al");
            while(s.charAt(i - 1) != ':') i++;
            sb = new StringBuilder();
            for(; s.charAt(i) != ']'; i++)
            {
                sb.append(s.charAt(i));
            }
            album = sb.toString();

            lyric = s.substring(s.indexOf("[0"));
        }
    }

    public int getTimeMillis()
    {
        double time = 0;

one:    for(int i = lyric.indexOf('['); i < lyric.length(); i++)
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
//        Log.e("tag0", time+"");
        return (int) time;
    }

    public String getLyric()
    {
        return lyric.substring(lyric.indexOf(']') + 1);
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }
}
