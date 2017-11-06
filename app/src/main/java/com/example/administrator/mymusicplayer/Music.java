package com.example.administrator.mymusicplayer;

/**
 * Created by Administrator on 2017/10/30.
 */

public class Music
{
    private String location;

    Music(String location)
    {
        this.location = location;
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
