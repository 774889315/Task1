package com.example.administrator.mymusicplayer.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */

public class Music
{
    private String location;
    public List<Lyric> lyric;

    public Music(String location)
    {
        this.location = location;
        //找歌词文件是否存在
        File lyricFile = new File(location.substring(0, location.indexOf('.')) + ".lrc");
        if(lyricFile.exists())
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(lyricFile));
                String line;
                lyric = new ArrayList<>();
                while((line = br.readLine()) != null)
                {
          //          Log.e("line", line);
    //                if(line.charAt(0) == '[')
                    {
                        lyric.add(new Lyric(line));
 //                       Log.e("tag0", line);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getLocation()
    {
        return location;
    }

    public String getName()
    {
        return location.substring(location.lastIndexOf("/") + 1);
    }
}
