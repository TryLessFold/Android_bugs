package com.example.bugs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.concurrent.Semaphore;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private int height;
    private int width;

    private GameThread gameThread;

    private Bug[] bugs = new Bug[10];
    private Bug bug;

    private static final int MAX_STREAMS=10;
    private int soundIdhit;
    private int soundIdMiss;

    private boolean soundPoolLoaded;
    private SoundPool soundPool;

    private MediaPlayer player;

    private Semaphore sem = new Semaphore(1);

    public GameSurface(Context context, int width, int height)  {
        super(context);

        this.height = height;
        this.width = width;

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        this.initSoundPool();
    }

    private void initSoundPool()  {
        player = MediaPlayer.create(this.getContext(), R.raw.background);
        player.start();
        player.setLooping(true);
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS).build();

            this.soundPool = builder.build();
        }

        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;
            }
        });

        this.soundIdMiss= this.soundPool.load(this.getContext(), R.raw.miss,1);

        this.soundIdhit = this.soundPool.load(this.getContext(), R.raw.hit,1);


    }

    public void playSoundHit()  {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            int streamId = this.soundPool.play(this.soundIdhit,leftVolumn, rightVolumn, 1, 0, 2f);
        }
    }

    public void playSoundMiss()  {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            int streamId = this.soundPool.play(this.soundIdMiss,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void update()  {
        for(int i = 0; i < 10; i ++)
        {
            this.bugs[i].update();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res;
        int flag = 0;
//        try{
//            sem.acquire();
//        } catch (InterruptedException e) {}
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int y = (int) event.getY();
            int x = (int) event.getX();
            for (int i = 0; i < 10; i++) {
                if (this.bugs[i].killCheck(x, y) == 1) {
                    bugs[i].killBug();
                    flag = 1;
                }
            }
            if (flag == 1) {
                playSoundHit();
            } else playSoundMiss();
            res = true;
        }
        res = false;
        return res;
    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);
        for(int i = 0; i < 10; i ++) {
            this.bugs[i].draw(canvas);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap bugsBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bugs);
        for(int i = 0; i < 10; i ++)
        {
            this.bugs[i] = new Bug(this, bugsBitmap, 3, 3, 100 + i *50, 30, sem);
        }
        this.gameThread = new GameThread(this,holder, sem);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            }catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }

    public int getWinWidth() {
        return width;
    }

    public int getWinHeight() {
        return height;
    }
}