package com.example.planegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    Button replayButton, returnButton;
    TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        replayButton = findViewById(R.id.replayButton);
        returnButton = findViewById(R.id.returnButton);
        scoreText = findViewById(R.id.scoreText);

        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        boolean result = intent.getBooleanExtra("result", false);
        scoreText.setText((result ? "You Win! " : "You Lose! ") + String.valueOf(score));

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultActivity.this, GameActivity.class));
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultActivity.this, MainActivity.class));
            }
        });
    }
}
