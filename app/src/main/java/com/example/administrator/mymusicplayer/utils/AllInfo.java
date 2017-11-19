package com.example.administrator.mymusicplayer.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/17.
 */

public class AllInfo implements Serializable
{
    public List<Lyric> lyric = new ArrayList<>();
    public List<String> link = new ArrayList<>();
}
