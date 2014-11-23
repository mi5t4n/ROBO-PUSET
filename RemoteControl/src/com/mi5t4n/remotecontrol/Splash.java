package com.mi5t4n.remotecontrol;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class Splash extends Activity implements OnClickListener{
	MediaPlayer splashMusic;
	LinearLayout llSplash;
	Thread timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		llSplash = (LinearLayout) findViewById(R.id.llSplash);
		llSplash.setOnClickListener(this);

		splashMusic = MediaPlayer.create(Splash.this, R.raw.splash);
		splashMusic.start();

		timer = new Thread() {
			public void run() {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					Intent i = new Intent("android.intent.action.MAINACTIVITY");
					startActivity(i);
				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		splashMusic.release();
		finish();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.llSplash:
			timer.interrupt();
			timer=null;
			Intent i = new Intent("android.intent.action.MAINACTIVITY");
			startActivity(i);
		}
	}
}
