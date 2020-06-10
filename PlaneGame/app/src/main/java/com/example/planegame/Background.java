package com.example.planegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background extends GameObject{

    public static int speed = 10;

    Background(int scX, int scY, Resources res) {
        super(0, 0, res, R.drawable.bg);
        super.scale(scX, scY, 1);
    }
}
