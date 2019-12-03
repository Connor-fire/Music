package com.example.music;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //在子线程中通过handler的post(Runnable)方法更新进度条和正在播放的音乐。
    private Handler myHandler = new Handler();

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private TextView timeTextView;
    private TextView music_name;
    private MyDatabaseHelper dbHelper;

    private SimpleDateFormat time = new SimpleDateFormat("m:ss");//mm分:ss秒

    private int i = 0;

    File pFile = Environment.getExternalStorageDirectory();//SD卡根目录
    //歌曲路径
    private String[] musicPath = new String[]{
            pFile + "/Download/music.mp3",
            pFile + "/Download/music2.mp3",
            pFile + "/Download/music3.mp3",
            pFile + "/Download/music4.mp3"
    };
    final String[] data = new String[]{"music.mp3","music2.mp3","music3.mp3","music4.mp3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button play = findViewById(R.id.play);
        Button stop = findViewById(R.id.stop);
        Button pause = findViewById(R.id.pause);
        Button nextMusic = findViewById(R.id.next);
        Button preMusic = findViewById(R.id.previous);
        Button mymusic = findViewById(R.id.mymusic);

        dbHelper =new MyDatabaseHelper(this,"Music.db",null ,1);

        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        pause.setOnClickListener(this);
        nextMusic.setOnClickListener(this);
        preMusic.setOnClickListener(this);
        mymusic.setOnClickListener(this);


        seekBar = findViewById(R.id.seekbar);
        timeTextView = findViewById(R.id.text1);
        music_name = findViewById(R.id.music_name);

        //运行时权限处理，动态申请WRITE_EXTERNAL_STORAGE权限
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            initMediaPlayer(0);
        }

        myHandler.post(update_music);


        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                MainActivity.this,android.R.layout.simple_list_item_1,data
        );
        ListView listView=(ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int j, long l) {
                i=j;
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.reset();
                    initMediaPlayer(i);
                }
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int j, long l) {
                //Toast.makeText(MainActivity.this,"长按",Toast.LENGTH_SHORT).show();

                final int index=j;
                AlertDialog.Builder dialog= new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("添加提醒");
                dialog.setMessage("确定要将所选音乐添加至歌单？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String Name = data[index];
                        String Path = musicPath[index];
                        //Toast.makeText(MainActivity.this,i+Name+"  "+Path,Toast.LENGTH_LONG).show();
                        try{
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("Name",Name);
                            values.put("Path",Path);
                            //Toast.makeText(MainActivity.this,Name+"  "+Path,Toast.LENGTH_SHORT).show();
                            db.insert("Music",null,values);
                            Toast.makeText(MainActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"添加失败",Toast.LENGTH_SHORT).show();
                        }

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


    }


    private void initMediaPlayer(int musicIndex){
        try {
            mediaPlayer.setDataSource(musicPath[musicIndex]);//指定音频文件路径
            mediaPlayer.prepare();
        }catch(Exception e){
            e.printStackTrace();
        }


        seekBar.setMax(mediaPlayer.getDuration());
        //拖动进度条事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    @Override
    //拒绝权限获取则直接关闭程序
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer(0);
                }else{
                    Toast.makeText(this,"未授予权限，无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                break;
            case R.id.pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
            case R.id.stop:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.reset();
                    initMediaPlayer(i);
                }
                break;
            case R.id.next:
                playNextMusic();
                break;
            case R.id.previous:
                playPreMusic();
                break;
            case R.id.mymusic:
                Intent intent =new Intent(MainActivity.this,Mymusic.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    private void playNextMusic(){
        if(mediaPlayer != null && i < 4 && i >=0){
            mediaPlayer.reset();
            switch (i){
                case 0: case 1: case 2:
                    initMediaPlayer(i+1);
                    i = i + 1;
                case 3:
                    initMediaPlayer(3);
            }
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }

    private void playPreMusic(){
        if(mediaPlayer != null && i < 4 && i >=0){
            mediaPlayer.reset();
            switch (i){
                case 1: case 2: case 3:
                    initMediaPlayer(i-1);
                    i = i - 1;
                case 0:
                    initMediaPlayer(0);
            }
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }

    //更新UI
    private Runnable update_music = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            timeTextView.setText(time.format(mediaPlayer.getCurrentPosition()) + "s");//将给定时间转化为指定格式
            music_name.setText("当前正在播放："+data[i]);//当前正在播放
            myHandler.postDelayed(update_music,1000);//UI的更新周期为1s
        }

    };


    //释放资源
    protected void onDestroy(){
        super.onDestroy();
        myHandler.removeCallbacks(update_music);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }




}
