package com.example.planegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable{

    //Screen settings
    private int frameRate = 60;
    private int interval = 1000 / frameRate;
    public static int scX, scY;
    public static float scRatioX, scRatioY;

    private static Random random = new Random();
    private Thread thread;

    //Game objects
    private Background bg1, bg2;
    private Plane plane;
    private Paint paint;
    private List<Bullet> bullets;
    private Pig pig;
    private List<Bird> birds;
    private Bitmap[] hearts;

    //Game settings
    private boolean isPlaying;
    private boolean win = false;
    private boolean fail = false;

    public GameView(Context context, int scX, int scY) {
        super(context);
        GameView.scX = scX;
        GameView.scY = scY;
        scRatioX = 1920f / scX;
        scRatioY = 1080f / scY;
        paint = new Paint();

        //Initialize game objects
        bg1 = new Background(scX, scY, getResources());
        bg2 = new Background(scX, scY, getResources());
        bg1.x = 0;
        bg2.x = scX;
        plane = new Plane(scY, getResources());
        pig = new Pig(getResources());
        Bird.pig = pig;

        //Hearts bitmap for lives
        hearts = new Bitmap[5];
        for (int i = 0; i < 5; i++) {
            hearts[i] = BitmapFactory.decodeResource(getResources(), R.drawable.live);
            hearts[i] = Bitmap.createScaledBitmap(hearts[i], (int)(hearts[i].getWidth() * GameView.scRatioX / 15), (int)(hearts[i].getHeight() * GameView.scRatioY / 15), false);
        }

        //Bullets
        bullets = new ArrayList<Bullet>();
        for (int i = 0; i < 15; i++) {
            Bullet bullet = new Bullet(plane.x + plane.width, plane.y + plane.height / 2, getResources());
            bullet.x -= (i << 1) * bullet.width;
            bullets.add(bullet);
        }

        //Birds
        birds = new ArrayList<Bird>();
        for (int i = 0; i < 10; i++) {
            Bird bird = new Bird(i >> 1, getResources());
            bird.shuffle();
            bird.x += i * bird.width;
            birds.add(bird);
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            //Update game objects
            update();
            //Draw game objects
            draw();
            //Sleep in each frame
            sleep();
        }
        //Switch to the result activity
        Intent intent = new Intent();
        intent.putExtra("score", plane.score);
        intent.putExtra("result", win);
        intent.setClass(getContext(), ResultActivity.class);
        getContext().startActivity(intent);
    }

    private void update() {
        //Update the backgrounds to implement sliding effect
        bg1.x -= (int)(Background.speed * scRatioX);
        bg2.x -= (int)(Background.speed * scRatioX);
        if (bg1.x + bg1.width < 0) {
            bg1.x = scX;
        }
        if (bg2.x + bg2.width < 0) {
            bg2.x = scX;
        }

        //Update the plane
        if (plane.mode == Plane.MODE.UP) {
            plane.y -= (int)(Plane.speed * scRatioY);
        } else if (plane.mode == Plane.MODE.DOWN) {
            plane.y += (int)(Plane.speed * scRatioY);
        }
        plane.y = Math.max(plane.y, 0);
        plane.y = Math.min(plane.y, scY - plane.height);

        //Update the pig
        if (pig.direction) {
            pig.y -= (int)(Pig.speed * scRatioY);
        } else {
            pig.y += (int)(Pig.speed * scRatioY);
        }
        pig.y = Math.max(pig.y, 0);
        pig.y = Math.min(pig.y, scY - pig.height);
        Pig.speed = 10 + random.nextInt(10);
        if (pig.y < (pig.height >> 1)) {
            pig.direction = false;
        } else if (pig.y > scY - pig.height * 1.5f) {
            pig.direction = true;
        }
        if (plane.y + (plane.height >> 1) >= pig.y && plane.y + (plane.height >> 1) <= pig.y + pig.height) {
            pig.direction = !pig.direction;
        }

        //Update the bullets
        for (Bullet bullet : bullets) {
            bullet.x += (int)(Bullet.speed * scRatioX);
            if (bullet.x < plane.x + plane.width) {
                bullet.y = plane.y + (plane.height >> 1) - (bullet.height >> 1);
            }
            if (bullet.x > scX) {
                bullet.x = plane.x + plane.width;
                bullet.y = plane.y + (plane.height >> 1) - (bullet.height >> 1);
            }
            for (Bird bird : birds) {
                if (!bullet.dead && Rect.intersects(bullet.getCollier(), bird.getCollier())) {
                    bird.dead = true;
                    bullet.dead = true;
                    plane.score++;
                    break;
                }
            }
            if (!bullet.dead && Rect.intersects(bullet.getCollier(), pig.getCollier())) {
                pig.hp--;
                bullet.dead = true;
                if (pig.hp <= 0) {
                    plane.score += 20;
                    pig.dead = true;
                    win = true;
                    return;
                }
            }
        }

        //Update the birds
        for (Bird bird : birds) {
            bird.x -= (int)(bird.speed * scRatioX);
            if (bird.x > pig.x) {
                bird.y = pig.y + (pig.height >> 1) - (bird.height >> 1);
            }
            if (bird.x + bird.width < 0) {
                bird.shuffle();
            }
            if (Rect.intersects(bird.getCollier(), plane.getCollier())) {
                plane.hp--;
                bird.dead = true;
                if (plane.hp <= 0) {
                    plane.dead = true;
                    fail = true;
                    return;
                }
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            //Draw the backgrounds
            canvas.drawBitmap(bg1.bmp, bg1.x, bg1.y, paint);
            canvas.drawBitmap(bg2.bmp, bg2.x, bg2.y, paint);

            if (fail) {
                isPlaying = false;
                int boomX = plane.x + (plane.width >> 1) - (plane.boom.getWidth() >> 1);
                int boomY = plane.y + (plane.height >> 1) - (plane.boom.getHeight() >> 1);
                canvas.drawBitmap(plane.boom, boomX, boomY, paint);
                getHolder().unlockCanvasAndPost(canvas);
                return;
            }
            if (win) {
                isPlaying = false;
                int boomX = pig.x + (pig.width >> 1) - (pig.boom.getWidth() >> 1);
                int boomY = pig.y + (pig.height >> 1) - (pig.boom.getHeight() >> 1);
                canvas.drawBitmap(pig.boom, boomX, boomY, paint);
                getHolder().unlockCanvasAndPost(canvas);
                return;
            }

            //Draw the bullets
            for (Bullet bullet : bullets) {
                if (bullet.dead) {
                    bullet.y = -10 - bullet.height;
                    bullet.dead = false;
                } else if (plane.x + plane.width <= bullet.x && bullet.x < scX) {
                    canvas.drawBitmap(bullet.bmp, bullet.x, bullet.y, paint);
                }
            }

            //Draw the birds
            for (Bird bird : birds) {
                if (bird.dead) {
                    int boomX = bird.x + (bird.width >> 1) - (bird.boom.getWidth() >> 1);
                    int boomY = bird.y + (bird.height >> 1) - (bird.boom.getHeight() >> 1);
                    canvas.drawBitmap(bird.boom, boomX, boomY, paint);
                    bird.dead = false;
                    bird.shuffle();
                }else if (-bird.width <= bird.x && bird.x <= pig.x) {
                    canvas.drawBitmap(bird.bmp, bird.x, bird.y, paint);
                }
            }

            //Draw the plane
            canvas.drawBitmap(plane.bmp, plane.x, plane.y, paint);

            //Draw the pig
            canvas.drawBitmap(pig.bmp, pig.x, pig.y, paint);

            //Draw hearts
            for (int i = 0; i < plane.hp; i++) {
                canvas.drawBitmap(hearts[i], (int)(32 * GameView.scRatioX + i * (hearts[i].getWidth() + 16)), (int)(32 * GameView.scRatioY), paint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //Press
            case MotionEvent.ACTION_DOWN:
                if (event.getY() > scY >> 1) {
                    plane.mode = Plane.MODE.DOWN;
                } else {
                    plane.mode = Plane.MODE.UP;
                }
                break;
            //Release
            case MotionEvent.ACTION_UP:
                plane.mode = Plane.MODE.NONE;
                break;
        }

        return true;
    }
}

