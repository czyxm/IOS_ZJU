package com.example.planegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

//The base class of all the game objects
public class GameObject {
    public int x, y, width, height, speed;
    public boolean dead = false;
    public Bitmap bmp;

    GameObject(int x, int y, Resources res, final int resID) {
        bmp = BitmapFactory.decodeResource(res, resID);
        this.x = x;
        this.y = y;
        width = bmp.getWidth();
        height = bmp.getHeight();
    }

    //Scale the bitmap with "w, h, s"
    public void scale(int w, int h, int s) {
        width = (int)(w * GameView.scRatioX / s);
        height = (int)(h * GameView.scRatioY / s);
        bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
    }

    //Obtain the collision rectangle
    public Rect getCollier() {
        return new Rect(x, y, x + width, y + height);
    }
}
