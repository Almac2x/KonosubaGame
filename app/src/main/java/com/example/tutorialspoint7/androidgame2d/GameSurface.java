package com.example.tutorialspoint7.androidgame2d;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameSurface";

    private GameThread gameThread;

    private final List<ChibiCharacter> chibiList = new ArrayList<ChibiCharacter>();
    private final List<ChibiCharacter> chibiList2 = new ArrayList<ChibiCharacter>();
    private final List<Explosion> explosionList = new ArrayList<Explosion>();
    private final List<Explosion> explosionList2 = new ArrayList<Explosion>();

    private static final int MAX_STREAMS=100;
    private int soundIdExplosion;
    private int soundIdBackground;
    private Sensor accelerometer;
    private Sensor proximity;
    private SensorManager sensorManager;


    private boolean soundPoolLoaded;
    private SoundPool soundPool;

    Socket socket;
    DataInputStream input;
    DataOutputStream out;

    Bitmap background;
    Rect rect;
    int dWidth,dHeight;

    DataInputStream input2;
    DataOutputStream out2;

    DataInputStream input3;
    DataOutputStream out3;
    DataInputStream input4;
    DataOutputStream out4;
    int Player1Score=0,Player2Score=0;

    private TextView player1,player2;

    int SensorY=0,SensorX=0,SensorY2=0,SensorX2=0;





    public GameSurface(Context context,TextView player1, TextView player2)  {
        super(context);
        background=BitmapFactory.decodeResource(getResources(),R.drawable.bg);
        Display display=((Activity)getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth= size.x;
        dHeight=size.y;
        rect=new Rect(0,0,dWidth,dHeight);
        this.player1 = player1;
        this.player2 = player2;


        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(sensorEventListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener,proximity,SensorManager.SENSOR_DELAY_NORMAL);

       RunSocket runSocket = new RunSocket("192.168.43.151","2000");//Connect to Server
       new Thread(runSocket).start();

        RunSocket2 runSocket2 = new RunSocket2("192.168.43.151","5000");//Connect to Server
        new Thread(runSocket2).start();

        //RunSocket runSocket3 = new RunSocket("192.168.43.151","5001");//Connect to Server
        //new Thread(runSocket3).start();


        //RunSocket runSocket4 = new RunSocket("192.168.43.151","5002");//Connect to Server
        //new Thread(runSocket4).start();

    // RunSocket runSocket2 = new RunSocket("192.168.43.151","5000");//Connect to Server
        // new Thread(runSocket).start();






        // Make Game Surface focusable so it can handle events.

        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);

        this.initSoundPool();


    }

    private void initSoundPool()  {
        // With Android API >= 21.
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // With Android API < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When SoundPool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;

                // Playing background sound.
                playSoundBackground();
            }
        });

        // Load the sound background.mp3 into SoundPool
        this.soundIdBackground= this.soundPool.load(this.getContext(), R.raw.tombitchtune,1);

        // Load the sound explosion.wav into SoundPool
        this.soundIdExplosion = this.soundPool.load(this.getContext(), R.raw.meguminsound2,1);


    }

    public void playSoundExplosion() {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            // Play sound explosion.wav
            int streamId = this.soundPool.play(this.soundIdExplosion,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playSoundBackground()  {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            // Play sound background.mp3
            int streamId = this.soundPool.play(this.soundIdBackground,leftVolumn, rightVolumn, 1, -1, 1f);
        }
    }

   /*@Override //Touch Movement
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            int x=  (int)event.getX();
            int y = (int)event.getY();
            Log.d(TAG, "onTouchEvent X: "+x+ " Y: "+y);
            Iterator<ChibiCharacter> iterator= this.chibiList.iterator();


            while(iterator.hasNext()) {
                ChibiCharacter chibi = iterator.next();
                if( chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
                        && chibi.getY() < y && y < chibi.getY()+ chibi.getHeight())  {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();

                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }


            for(ChibiCharacter chibi: chibiList) {
                int movingVectorX =x-  chibi.getX() ;
                int movingVectorY =y-  chibi.getY() ;
                chibi.setMovingVector(movingVectorX, movingVectorY);
            }
            return true;
        }
        return false;
    }*/

   @Override //Explosion Event
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            int x=  (int)event.getX();
            int y = (int)event.getY();
            Log.d(TAG, "onTouchEvent X: "+x+ " Y: "+y);
            Iterator<ChibiCharacter> iterator= this.chibiList.iterator();
            Iterator<ChibiCharacter> iterator2= this.chibiList2.iterator();


            while(iterator.hasNext()) { //Explosion chibilist1
                ChibiCharacter chibi = iterator.next();
                if( chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
                        && chibi.getY() < y && y < chibi.getY()+ chibi.getHeight())  {
                    // Remove the current element from the iterator and the list.

                    Player1Score++;
                    player1.setText("Player1= " +Integer.toString(Player1Score));






                    Log.d(TAG, "onTouchEvent: "+Player1Score);
                    iterator.remove();

                    if(chibiList.isEmpty()){ //Adds if All chibis are gone
                        Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.kazuma);
                        ChibiCharacter chibi1 = new ChibiCharacter(this,chibiBitmap1,100,50);

                        Bitmap chibiBitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.megumin);
                        ChibiCharacter chibi2 = new ChibiCharacter(this,chibiBitmap2,300,150);

                        this.chibiList.add(chibi1);
                        this.chibiList.add(chibi2);

                    }
                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }


            while(iterator2.hasNext()) {//explosion chibilist 2
                ChibiCharacter chibi = iterator2.next();
                if( chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
                        && chibi.getY() < y && y < chibi.getY()+ chibi.getHeight())  {
                    // Remove the current element from the iterator and the list.
                   // player2.setText(player2.getText()+" " +Integer.toString(Player2Score));
                    iterator2.remove();

                    Player2Score++;
                    player2.setText("Player2= " +Integer.toString(Player2Score));

                    if(chibiList2.isEmpty()){ //Adds if All chibis are gone
                        Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.aqua);
                        ChibiCharacter chibi1 = new ChibiCharacter(this,chibiBitmap1,450,300);

                        Bitmap chibiBitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.darkness);
                        ChibiCharacter chibi2 = new ChibiCharacter(this,chibiBitmap2,300,450);

                        this.chibiList2.add(chibi1);
                        this.chibiList2.add(chibi2);

                    }
                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }



            for(ChibiCharacter chibi: chibiList) {
                int movingVectorX =x-  chibi.getX() ;
                int movingVectorY =y-  chibi.getY() ;

            }

            for(ChibiCharacter chibi: chibiList2) {
                int movingVectorX =x-  chibi.getX() ;
                int movingVectorY =y-  chibi.getY() ;

            }
            return true;
        }
        return false;
    }

   //Gyroscope Movement
    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                Iterator<ChibiCharacter> iterator= chibiList.iterator();
                Iterator<ChibiCharacter> iterator2= chibiList2.iterator();



                final int SensorX2 = (int)sensorEvent.values[0];
                final int SensorY2 = (int)sensorEvent.values[1];

                /*Thread thread = new Thread(new Runi(sensorEvent));
                thread.setPriority(9);
                thread.start();*/


/////// java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.DataOutputStream.writeInt(int)' on a null object reference
//        at com.example.tutorialspoint7.androidgame2d.GameSurface$2$1.run(GameSurface.java:332)

               new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {


                            Thread.sleep(100);


                            SensorY = input.readInt();
                            SensorX = input2.readInt();





                            if(SensorY>5){
                                SensorY=5;
                            }
                            else if(SensorY<-5){
                                SensorY=-5;
                            }

                            if(SensorX>5){
                                SensorX=5;
                            }
                            else if(SensorY<-5){
                                SensorY=-5;
                            }



                            Log.d(TAG, "EYYYYYYYYYYYYYYYYYYYYY: "+SensorY);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }
                }).start();




                int x = 0;
                for(ChibiCharacter chibi: chibiList) {
                    if(x==0){
                        int movingVectorX =chibi.getX() ;
                        int movingVectorY =chibi.getY() ;
                        chibi.setMovingVector(SensorY, SensorX); //Sends X and Y sensor Values
                        // Log.d(TAG, "onSensorChanged X: "+Float.toString(sensorEvent.values[0]) +" Z: "+Float.toString(sensorEvent.values[1]) );
                    }
                    x++;
                }
                int i = 0;
                for(ChibiCharacter chibi: chibiList2) {
                    if(i==0){
                        int movingVectorX =chibi.getX() ;
                        int movingVectorY =chibi.getY() ;
                        chibi.setMovingVector(SensorY2, SensorX2        ); //Sends X and Y sensor Values
                        // Log.d(TAG, "onSensorChanged X: "+Float.toString(sensorEvent.values[0]) +" Z: "+Float.toString(sensorEvent.values[1]) );
                    }
                    i++;
                }
            }
            if(sensorEvent.sensor.getType()==Sensor.TYPE_PROXIMITY){

                int SensorX = (int)sensorEvent.values[0];

                int x=0;
                for(ChibiCharacter chibi: chibiList) {
                    if(x==1){
                        int movingVectorX =chibi.getX() ;
                        int movingVectorY =chibi.getY() ;
                        chibi.setMovingVector(0, SensorX); //Sends X and Y sensor Values
                        Log.d(TAG, "Proximity X: "+Float.toString(sensorEvent.values[0])  );
                    }
                    x++;
                }
                int i=0;
                for(ChibiCharacter chibi: chibiList2) {
                    if(x==1){
                        int movingVectorX =chibi.getX() ;
                        int movingVectorY =chibi.getY() ;
                        chibi.setMovingVector(0, SensorX); //Sends X and Y sensor Values
                        Log.d(TAG, "Proximity X: "+Float.toString(sensorEvent.values[0])  );
                    }
                    i++;
                }


            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    public void update()  {
        for(ChibiCharacter chibi: chibiList) {
            chibi.update();
        }
        for(ChibiCharacter chibi: chibiList2) {
            chibi.update();
        }
        for(Explosion explosion: this.explosionList)  {
            explosion.update();
        }
        for(Explosion explosion: this.explosionList2)  {
            explosion.update();
        }

        Iterator<Explosion> iterator= this.explosionList.iterator();
        Iterator<Explosion> iterator2= this.explosionList2.iterator();
        while(iterator.hasNext())  {
            Explosion explosion = iterator.next();

            if(explosion.isFinish()) {
                // If explosion finish, Remove the current element from the iterator & list.
                iterator.remove();
                continue;
            }
        }
    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);
        canvas.drawBitmap(background,null,rect,null);
        for(ChibiCharacter chibi: chibiList)  {
            chibi.draw(canvas);
        }
        for(ChibiCharacter chibi: chibiList2)  {
            chibi.draw(canvas);
        }

        for(Explosion explosion: this.explosionList)  {
            explosion.draw(canvas);
        }
        for(Explosion explosion: this.explosionList2)  {
            explosion.draw(canvas);
        }

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.kazuma);
        ChibiCharacter chibi1 = new ChibiCharacter(this,chibiBitmap1,100,50);

        Bitmap chibiBitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.megumin);
        ChibiCharacter chibi2 = new ChibiCharacter(this,chibiBitmap2,300,150);

        this.chibiList.add(chibi1);
        this.chibiList.add(chibi2);

        Bitmap chibiBitmap3 = BitmapFactory.decodeResource(this.getResources(),R.drawable.aqua);
        ChibiCharacter chibi3 = new ChibiCharacter(this,chibiBitmap3,450,300);

        Bitmap chibiBitmap4 = BitmapFactory.decodeResource(this.getResources(),R.drawable.darkness);
        ChibiCharacter chibi4 = new ChibiCharacter(this,chibiBitmap4,300,450);

        this.chibiList2.add(chibi3);
        this.chibiList2.add(chibi4);



        this.gameThread = new GameThread(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            }catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }

    //WebSocket Connect to Sever
    public class RunSocket implements Runnable{
        String IpAddress,Port;


        RunSocket(String IpAddress,String Port){

            this.IpAddress = IpAddress;
            this.Port = Port;
        }


        @Override
        public void run() {



            try
            {
                socket = new Socket(IpAddress, Integer.parseInt(Port));
                if(socket.isConnected()){
                    Log.d(TAG, "Status: "+ "Connected");
                }

                // takes input from terminal
                input = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));


                // sends output to the socket
                out    = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                Log.d(TAG, "Host: "+u);
            }
            catch(IOException i)
            {
                Log.d(TAG, "Input: "+i);
            }

        }
    }

    public class RunSocket2 implements Runnable{
        String IpAddress,Port;


        RunSocket2(String IpAddress,String Port){

            this.IpAddress = IpAddress;
            this.Port = Port;
        }


        @Override
        public void run() {



            try
            {
                socket = new Socket(IpAddress, Integer.parseInt(Port));
                if(socket.isConnected()){
                    Log.d(TAG, "Status: "+ "Connected");
                }

                // takes input from terminal
                input2 = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));


                // sends output to the socket
                out2   = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                Log.d(TAG, "Host: "+u);
            }
            catch(IOException i)
            {
                Log.d(TAG, "Input: "+i);
            }

        }
    }
    public class RunSocket3 implements Runnable{
        String IpAddress,Port;


        RunSocket3(String IpAddress,String Port){

            this.IpAddress = IpAddress;
            this.Port = Port;
        }


        @Override
        public void run() {



            try
            {
                socket = new Socket(IpAddress, Integer.parseInt(Port));
                if(socket.isConnected()){
                    Log.d(TAG, "Status: "+ "Connected");
                }

                // takes input from terminal
                input3 = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));


                // sends output to the socket
                out3   = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                Log.d(TAG, "Host: "+u);
            }
            catch(IOException i)
            {
                Log.d(TAG, "Input: "+i);
            }

        }
    }
    public class RunSocket4 implements Runnable{
        String IpAddress,Port;


        RunSocket4(String IpAddress,String Port){

            this.IpAddress = IpAddress;
            this.Port = Port;
        }


        @Override
        public void run() {



            try
            {
                socket = new Socket(IpAddress, Integer.parseInt(Port));
                if(socket.isConnected()){
                    Log.d(TAG, "Status: "+ "Connected");
                }

                // takes input from terminal
                input4 = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));


                // sends output to the socket
                out4  = new DataOutputStream(socket.getOutputStream());
            }
            catch(UnknownHostException u)
            {
                Log.d(TAG, "Host: "+u);
            }
            catch(IOException i)
            {
                Log.d(TAG, "Input: "+i);
            }

        }
    }
    public void toastMaker(String Message){
        Toast.makeText(getContext(),Message, Toast.LENGTH_SHORT).show();
    }

    public  int getPlayer1Score(){
        return Player1Score;
    }
    public class Runi implements Runnable{
        SensorEvent sensorEvent;

        Runi(SensorEvent event){

            sensorEvent = event;

        }


        @Override
        public void run() {
            try {

                SensorY = input.readInt();
                SensorX = input2.readInt();



                if(SensorY>2){
                    SensorY=2;
                }
                else if(SensorY<-2){
                    SensorY=-2;
                }

                if(SensorX>2){
                    SensorX=2;
                }
                else if(SensorY<-2){
                    SensorY=-2;
                }

                Log.d(TAG, "EYYYYYYYYYYYYYYYYYYYYY: "+SensorY);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

}