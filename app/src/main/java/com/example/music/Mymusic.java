package com.example.music;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mymusic extends AppCompatActivity implements View.OnClickListener {

    private Handler myHandler1 = new Handler();

    private SeekBar seekBar1;
    private TextView timeTextView1;
    private TextView music_name1;

    private MyDatabaseHelper dbHelper;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    ArrayList name_lsit = new ArrayList();
    ArrayList path_lsit = new ArrayList();
    private int i=0;

    private SimpleDateFormat time = new SimpleDateFormat("m:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymusic);

        dbHelper = new MyDatabaseHelper(this, "Music.db", null, 1);

        Button back = findViewById(R.id.back_btn);
       // Button cre_db = findViewById(R.id.cre_db_btn);//建库
        Button pause_btn = findViewById(R.id.pause_btn);
        Button play_btn = findViewById(R.id.play_btn);
        Button next_btn = findViewById(R.id.next_btn);
        Button pre_btn = findViewById(R.id.pre_btn);
        Button random_btn = findViewById(R.id.random_btn);
        Button order_btn=findViewById(R.id.order_btn);

        back.setOnClickListener(this);
        //cre_db.setOnClickListener(this);
        pause_btn.setOnClickListener(this);
        play_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        pre_btn.setOnClickListener(this);
        random_btn.setOnClickListener(this);
        order_btn.setOnClickListener(this);


        seekBar1 = findViewById(R.id.seekbar1);
        timeTextView1 = findViewById(R.id.text11);
        music_name1 = findViewById(R.id.music_name1);


        if(ContextCompat.checkSelfPermission(Mymusic.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Mymusic.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            //initMediaPlayer(0);
        }




        try {
            SQLiteDatabase db1 = dbHelper.getWritableDatabase();
            //Cursor cursor = db.rawQuery("Select * from Word", null);
            String[] coiumns = {"Name", "Path"};
            Cursor cursor = db1.query("Music", coiumns, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {

                    name_lsit.add(cursor.getString(0));
                    path_lsit.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                Mymusic.this, android.R.layout.simple_list_item_1, name_lsit
        );
        ListView listView1 = (ListView) findViewById(R.id.list_view1);
        listView1.setAdapter(adapter);


        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int j, long l) {
                i=j;
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.reset();
                    initMediaPlayer(i);
                }
                if(!mediaPlayer.isPlaying()){
                    initMediaPlayer(i);
                    mediaPlayer.start();
                }
            }
        });

        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int j, long l) {


                final int index1=j;
                AlertDialog.Builder dialog= new AlertDialog.Builder(Mymusic.this);
                dialog.setTitle("删除提醒！");
                dialog.setMessage("确定要删除所选音乐？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {
                        String Name=name_lsit.get(index1).toString();
                        String Path=path_lsit.get(index1).toString();
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("Music","Name = ? AND Path = ?",new String[]{Name,Path});
                        Toast.makeText(Mymusic.this,"已删除",Toast.LENGTH_SHORT).show();
                        try{
                            name_lsit.clear();
                            path_lsit.clear();
                            SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                            String[] coiumns={"Name","Path"};
                            Cursor cursor =db1.query("Music",coiumns,null,null,null,null,null);
                            if(cursor.moveToFirst()){
                                do{
                                    name_lsit.add(cursor.getString(0));
                                    path_lsit.add(cursor.getString(1));

                                }while(cursor.moveToNext());
                            }
                            cursor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ListView listView1 = (ListView) findViewById(R.id.list_view1);
                        listView1.setAdapter(adapter);

                        i=0;
                    }

                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
                return true;
            }
        });

        try{
            myHandler1.post(update_mymusic);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                Intent intent = new Intent(Mymusic.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
//            case R.id.cre_db_btn:
//                dbHelper.getWritableDatabase();
//                break;
            case  R.id.play_btn:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                break;
            case R.id.pause_btn:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
            case R.id.next_btn:
                if (i+1<=name_lsit.size()-1){
                    i=i+1;
                    mediaPlayer.reset();
                    initMediaPlayer(i);
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }
                break;
            case R.id.pre_btn:
                if(i-1>=0){
                    i=i-1;
                    mediaPlayer.reset();
                    initMediaPlayer(i);
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }

                break;
            case R.id.random_btn:
                i=(int)(Math.random()*(name_lsit.size()-1));
                mediaPlayer.reset();
                initMediaPlayer(i);
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        int tmp=i;
                        do{
                            i=(int)(Math.random()*(name_lsit.size()-1));
                        }while(i!=tmp);//避免连续播放同一首歌曲

                            mediaPlayer.reset();
                            initMediaPlayer(i);
                            if(!mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }


                    }
                });
            case R.id.order_btn:
                i=0;
                mediaPlayer.reset();
                initMediaPlayer(i);
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        i++;
                        if(i<=name_lsit.size()-1){
                            mediaPlayer.reset();
                            initMediaPlayer(i);
                            if(!mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }
                        }else{//到达列表末后返回列表首，列表循环播放
                            i=0;
                            mediaPlayer.reset();
                            initMediaPlayer(i);
                            if(!mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }
                        }
                    }
                });
            default:
                break;
        }
    }

    private void initMediaPlayer(int musicIndex) {
        try {
            mediaPlayer.setDataSource(path_lsit.get(musicIndex).toString());//指定音频文件路径
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        seekBar1.setMax(mediaPlayer.getDuration());
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //更新UI
    private Runnable update_mymusic = new Runnable() {
        @Override
        public void run() {
            seekBar1.setProgress(mediaPlayer.getCurrentPosition());
            timeTextView1.setText(time.format(mediaPlayer.getCurrentPosition()) + "s");
            music_name1.setText("当前正在播放："+name_lsit.get(i).toString());
            myHandler1.postDelayed(update_mymusic,1000);
        }

    };


    //释放资源
    protected void onDestroy(){
        super.onDestroy();
        myHandler1.removeCallbacks(update_mymusic);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}
