package com.example.bugs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceView;

import java.util.concurrent.Semaphore;

public class Bug {
    private GameSurface gameSurface;

    private long lastDrawNanoTime = -1;
    private long lastRouteUpdate = System.currentTimeMillis();
    private long dieTime = -1;

    private Bitmap image;
    private Bitmap sprite;
    private int width;
    private int height;

    private int size_x = 200;
    private int size_y = 200;

    private float x, y;
    private double dx, dy;

    private float velocity = 0.5f;

    Semaphore sem;

    Bug(GameSurface gameSurface, Bitmap image, int rowCount, int colCount, int x, int y, Semaphore sem) {
        this.gameSurface = gameSurface;
        this.width = image.getWidth() / rowCount;
        this.height = image.getHeight() / colCount;

        this.image = image;
        this.sprite = Bitmap.createBitmap(image, (int)(Math.random()*3)*width, (int)(Math.random()*3)*height,
                width, height);

        this.sprite = Bitmap.createScaledBitmap(this.sprite, size_x, size_y, false);

        this.x = x;
        this.y = y;

        dx = 0;
        dy = 0;

        this.sem = sem;
    }

    public void update(){
        long now = System.currentTimeMillis();
        long now_n = System.nanoTime();
        if (this.dieTime == -1) {
            int d = (int) ((now_n - lastDrawNanoTime) / 1000000);
            if ((now - this.lastRouteUpdate > 500) && (int) (Math.random() * 5) > 2) {
                dx = ((Math.random() * 2 > 1) ? 1 : -1);
                dy = ((Math.random() * 2 > 1) ? 1 : -1);
                // System.out.print(dx);
                this.lastRouteUpdate = System.currentTimeMillis();
            }

            try {
                sem.acquire();
                x += dx * velocity * d;
                y += dy * velocity * d;
            } catch (InterruptedException e)
            {
                System.out.println(e.getMessage());
            }

            sem.release();

            if (x + size_x > gameSurface.getWinWidth()) {
                x = gameSurface.getWinWidth() - size_x;
                dx *= -1;
            }
            if (y + size_y > gameSurface.getWinHeight()) {
                y = gameSurface.getWinHeight() - size_y;
                dy *= -1;
            }
            if (x < 0) {
                x = 0;
                dx *= -1;
            }
            if (y < 0) {
                y = 0;
                dy *= -1;
            }
        }
        else
        {
            if (now - this.dieTime > 1000)
            {
                respawnBug();
            }
        }
    }


    public void draw(Canvas canvas)  {
        try {
            sem.acquire();
            canvas.drawBitmap(sprite, x, y, null);
        }catch (InterruptedException e){}
        sem.release();

        this.lastDrawNanoTime= System.nanoTime();
    }

    public int killCheck(int x, int y)
    {
        int res = 0;
            if (x - this.x < size_x+50 && y - this.y < size_y+50 && x - this.x > -50 && y - this.y > -50) res = 1;
        return res;
    }

    public void killBug()
    {
        x = -2 * size_x;
        y = -2 * size_y;
        velocity = 0;
        this.dieTime = System.currentTimeMillis();
    }

    private void respawnBug()
    {
        this.sprite = Bitmap.createBitmap(image, (int)(Math.random()*3)*width, (int)(Math.random()*3)*height,
                width, height);

        this.sprite = Bitmap.createScaledBitmap(this.sprite, size_x, size_y, false);

        velocity = 0.5f;
        x = (float)Math.random() * gameSurface.getWinWidth();
        y = (float)Math.random() * gameSurface.getWinHeight();
        this.dieTime = -1;
    }

}
