package com.example.administrator.mymusicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Administrator on 2017/10/31.
 */

public class AddingPanel extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_panel);
        Button ok = (Button) findViewById(R.id.okay);
        final EditText input = (EditText) findViewById(R.id.path);
        input.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().indexOf('.') != -1) MainActivity.adapter.list.add(new Music(input.getText().toString()));
                else
                {
                    File dir = new File(input.getText().toString());
                    File[] file = dir.listFiles();
                    for(int i = 0; i < file.length; i++)
                    {
                        MainActivity.adapter.list.add(new Music(file[i].getPath()));
                    }
                }
                MainActivity.myList.setAdapter(MainActivity.adapter);
                finish();
            }
        });
    }
}
