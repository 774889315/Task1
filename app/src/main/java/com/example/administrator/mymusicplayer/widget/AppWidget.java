package com.example.administrator.mymusicplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.administrator.mymusicplayer.R;

public class AppWidget extends AppWidgetProvider {

    private final String ACTION_PLAY = "action_play";
    private final String ACTION_PAUSE = "action_pause";

    /**
     * 接受广播事件 
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(this.getClass().getName(), "onReceive");

        if (intent == null)
            return;

        String action = intent.getAction();

        if (action.equals(ACTION_PLAY) || action.equals(ACTION_PAUSE)) {
            // 只能通过远程对象来设置appWidget中的状态  
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);

     //       remoteViews.setTextViewText(R.id.play, ""+System.currentTimeMillis());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName componentName = new ComponentName(context, AppWidget.class);

            // 更新appWidget  
            appWidgetManager.updateAppWidget(componentName, remoteViews);
        }
    }

    /**
     * 到达指定的更新时间或者当用户向桌面添加AppWidget时被调用 
     * appWidgetIds:桌面上所有的widget都会被分配一个唯一的ID标识，这个数组就是他们的列表 
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(this.getClass().getName(), "onUpdate");

        Intent intent = new Intent(ACTION_PLAY);
        Intent intent1 = new Intent(ACTION_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, 0);

        // 小部件在Launcher桌面的布局  
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);

        // 事件
        remoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.pause, pendingIntent1);

        // 更新AppWidget  
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }


    /**
     * 删除AppWidget 
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(this.getClass().getName(), "onDeleted");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(this.getClass().getName(), "onDisabled");
    }

    /**
     * AppWidget首次创建调用 
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.e(this.getClass().getName(), "onEnabled");
    }
}  