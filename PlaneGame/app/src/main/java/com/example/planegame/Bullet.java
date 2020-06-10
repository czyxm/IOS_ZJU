package com.example.planegame;

import android.content.res.Resources;

public class Bullet extends GameObject{

    public static int speed = 30;

    Bullet(int x, int y, Resources res) {
        super(x, y, res, R.drawable.bullet);
        super.scale(width, height, 12);
    }
}
