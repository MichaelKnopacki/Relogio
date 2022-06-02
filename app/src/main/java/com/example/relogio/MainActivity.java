package com.example.relogio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewHolder mViewHolder = new ViewHolder();
    private Runnable mRunnable;
    private Handler mHandler = new Handler();
    private boolean mTicker = false;
    private boolean mLandscape = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //RECEBE O VALOR DA BATERIA
            int level = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, 0 );
            mViewHolder.textBattery.setText(String.format( "%s%%", level )); // EXIBE O VALOR DA BATERIA NA TELA
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        if(getSupportActionBar()!=null){
            getSupportActionBar().hide(); //ELIMINA A ACTION BAR
        }

        this.mViewHolder.textHourMinute = findViewById( R.id.text_hour_minute );
        this.mViewHolder.textSeconds = findViewById( R.id.text_seconds );
        this.mViewHolder.textBattery = findViewById( R.id.text_battery );
        this.mViewHolder.textNight = findViewById( R.id.text_night );

        this.mViewHolder.textHourMinute.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSystemUI();
            }
        } );

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver( this.mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) ); //QUANDO TEM ALTERAÇÃO DA BATERIA
        this.mTicker = true;

        this.mLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        this.startClock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mTicker = false;
        this.unregisterReceiver( this.mReceiver );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged( hasFocus );
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Esconde nav bar e status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    private void showSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    private void startClock(){

        Calendar calendar = Calendar.getInstance();

        this.mRunnable = new Runnable() { //Runnable é uma interface
            @Override
            public void run() {

                if (!mTicker) {
                    return;
                }

                calendar.setTimeInMillis( System.currentTimeMillis() );

                int hour = calendar.get( Calendar.HOUR_OF_DAY ); //CHAMA A HORA ATUAL
                int minutes = calendar.get( Calendar.MINUTE ); //CHAMA O MINUTO ATUAL
                int seconds = calendar.get( Calendar.SECOND ); // CHAMA O SEGUNDO ATUAL

                mViewHolder.textHourMinute.setText( String.format( Locale.getDefault(), "%02d:%02d", hour, minutes ) ); // INSTÂNCIA O VALOR DAS HORAS E MINUTOS
                mViewHolder.textSeconds.setText( String.format( Locale.getDefault(), "%02d", seconds ) );//INSTÂNCIA OS SEGUNDOS

                if (mLandscape) { //SE EU ACIONO O LANDSCAPE DEPOIS DAS 18hs ELE APRESENTE A MENSAGEM DE "BOA NOITE"
                    if (hour >= 18) {
                        mViewHolder.textNight.setVisibility( View.VISIBLE );
                    } else {
                        mViewHolder.textNight.setVisibility( View.GONE );
                    }

                    long now = SystemClock.elapsedRealtime();
                    long next = now + (1000 - (now % 1000)); // TEMPO EM QUE ELE ATUALIZA O RUNNABLE
                    mHandler.postAtTime( mRunnable, next );
                }
            }
        };
        this.mRunnable.run();
    }

    private static class ViewHolder{
        TextView textHourMinute;
        TextView textSeconds;
        TextView textBattery;
        TextView textNight;
    }
}