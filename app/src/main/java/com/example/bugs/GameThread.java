package com.example.bugs;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.Semaphore;

public class GameThread extends Thread {

    private boolean running;
    private GameSurface gameSurface;
    private SurfaceHolder surfaceHolder;
    Semaphore sem;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder, Semaphore sem)  {
        this.gameSurface= gameSurface;
        this.surfaceHolder= surfaceHolder;
        this.sem = sem;
    }

    @Override
    public void run()  {
        long startTime = System.nanoTime();

        while(running)  {
            Canvas canvas= null;
//            try{
//                sem.acquire();
//            } catch (InterruptedException e) {}
//            sem.release();
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (canvas)  {
                        this.gameSurface.update();
                        this.gameSurface.draw(canvas);
                }
            }catch(Exception e)  {
            } finally {
                if(canvas!= null)  {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            long now = System.nanoTime() ;
            long waitTime = (now - startTime)/1000000;
            if(waitTime < 10)  {
                waitTime= 10;
            }

            try {
                this.sleep(waitTime);
            } catch(InterruptedException e)  {

            }
            startTime = System.nanoTime();
            System.out.print(".");
        }
    }

    public void setRunning(boolean running)  {
        this.running= running;
    }
}