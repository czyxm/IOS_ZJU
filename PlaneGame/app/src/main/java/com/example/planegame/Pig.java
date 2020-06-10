package com.example.planegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Pig extends GameObject {

    public static int speed = 10;
    public int hp = 20;
    public boolean direction = true;
    public Bitmap boom;

    Pig(Resources res) {
        super(0, GameView.scY >> 1, res, R.drawable.pig);
        super.scale(width, height, 10);
        x = GameView.scX - width - (int)(8 * GameView.scRatioX);
        y -= height >> 1;
        boom = BitmapFactory.decodeResource(res, R.drawable.boom);
        boom = Bitmap.createScaledBitmap(boom, (int)(boom.getWidth() * GameView.scRatioX / 2), (int)(boom.getHeight() * GameView.scRatioY / 2), false);
    }
}
