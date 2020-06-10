package com.example.planegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Bird extends GameObject {

    public static Pig pig;
    //Five types of birds
    private static int[] resID = {R.drawable.bird1, R.drawable.bird2, R.drawable.bird3, R.drawable.bird4, R.drawable.bird5};

    private static Random random = new Random();
    public int speed = 15;
    public Bitmap boom;

    Bird(int type, Resources res) {
        super(GameView.scX, 0, res, resID[type]);
        super.scale(width, height, 5);
        boom = BitmapFactory.decodeResource(res, R.drawable.boom);
        boom = Bitmap.createScaledBitmap(boom, (int)(boom.getWidth() * GameView.scRatioX / 4), (int)(boom.getHeight() * GameView.scRatioY / 4), false);
    }

    //Reinitialize the bird's attributes
    public void shuffle() {
        //Randomize the speed of the bird
        speed = 20 + random.nextInt(9) - 4;
        //Initialize the position
        x = pig.x - width;
        y = pig.y + (pig.height >> 1) - (height >> 1);
    }
}
