package com.example.administrator.mymusicplayer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */

public class Music
{
    private String location;
    List<Lyric> lyric;

    Music(String location)
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
                    lyric.add(new Lyric(line));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    String getLocation()
    {
        return location;
    }

    String getName()
    {
        return location.substring(location.lastIndexOf("/") + 1);
    }
}
