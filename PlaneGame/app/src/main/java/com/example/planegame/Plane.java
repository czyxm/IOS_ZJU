package com.example.planegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Plane extends GameObject{

    public static int speed = 30;
    public Bitmap boom;
    public int score = 0;
    public int hp = 5;

    public enum MODE {
        UP, DOWN, NONE
    }
    public MODE mode = MODE.NONE;

    Plane(int scY, Resources res) {
        super((int)(64 * GameView.scRatioX), scY >> 1, res, R.drawable.plane);
        super.scale(width, height, 5);
        y -= height >> 1;
        boom = BitmapFactory.decodeResource(res, R.drawable.boom);
        boom = Bitmap.createScaledBitmap(boom, (int)(boom.getWidth() * GameView.scRatioX / 3), (int)(boom.getHeight() * GameView.scRatioY / 3), false);
    }
}
