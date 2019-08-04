package com.example.tutorialspoint7.androidgame2d;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

public class MainActivity extends Activity {

    Socket socket;
    InputStream input;
    DataOutputStream out;
    private static final String TAG = "MainActivity";

    TextView player1,player2;
    GameSurface gameSurface;
    int i=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(R.layout.activity_main);


        player1 = (TextView)findViewById(R.id.player1);
        player2 = (TextView)findViewById(R.id.player2);



        LinearLayout surface = findViewById(R.id.game);

        gameSurface = new GameSurface(this,player1,player2);
        surface.addView(gameSurface);








    }


    public void toastMaker(String Message){
        Toast.makeText(this,Message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {



        return false;

    }


}