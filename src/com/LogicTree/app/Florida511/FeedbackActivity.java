///////////////////////////////////////////////////////////////////////
//Copyright (C) 2012 Costas Kleopa.
//All Rights Reserved.
//
//Costas Kleopa, costas.kleopa@gmail.com
//
//This source code is the confidential property of Costas Kleopa.
//All proprietary rights, including but not limited to any trade
//secrets, copyright, patent or trademark rights in and to this source
//code are the property of Costas Kleopa. This source code is not to
//be used, disclosed or reproduced in any form without the express
//written consent of Costas Kleopa.
///////////////////////////////////////////////////////////////////////

package com.LogicTree.app.Florida511;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

public class FeedbackActivity extends Activity implements OnCompletionListener,
		SeekBar.OnSeekBarChangeListener {

	MediaRecorder recorder;
	File audiofile = null;
	private static final String TAG = "SoundRecordingActivity";
	private View startButton;
	private View stopButton;
	Uri newUri;
	private Utilities utils;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();
	private SeekBar songProgressBar;
	private MediaPlayer mp;
	String cuttrntSelect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.feedback);
		songProgressBar = (SeekBar) findViewById(R.id.seekBar1);
		songProgressBar.setOnSeekBarChangeListener(this); // Important
		
		final Button repTraffic = (Button) findViewById(R.id.btnReportTraffic);
		final Button repGeneral = (Button) findViewById(R.id.btnReportGeneral);
		repTraffic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
					cuttrntSelect = "Traffic";
					repTraffic.setEnabled(false);
					repGeneral.setEnabled(true);
			}
		});
		
		repGeneral.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
					cuttrntSelect = "Comment";
					repTraffic.setEnabled(true);
					repGeneral.setEnabled(false);
			}
		});

		Button sendData = (Button) findViewById(R.id.btnHomeHead);
		sendData.setText("Send");

		Button home = (Button) findViewById(R.id.btnGoBack);
		home.setVisibility(View.GONE);

		startButton = (Button) findViewById(R.id.btnRecord);
		stopButton = (Button) findViewById(R.id.btnStop);

		Button playSound = (Button) findViewById(R.id.btnPlay);
		playSound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mp = new MediaPlayer();
				utils = new Utilities();
				try {
					mp.setDataSource(String.valueOf(newUri));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					mp.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mp.start();

				// set Progress bar values
				songProgressBar.setProgress(0);
				songProgressBar.setMax(100);

				// Updating progress bar
				updateProgressBar();
			}
		});
	}

	public void startRecording(View view) throws IOException {

		startButton.setEnabled(false);
		stopButton.setEnabled(true);

		File sampleDir = Environment.getExternalStorageDirectory();
		try {
			audiofile = File.createTempFile("sound", ".3gp", sampleDir);
		} catch (IOException e) {
			Log.e(TAG, "sdcard access error");
			return;
		}
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(audiofile.getAbsolutePath());
		recorder.prepare();
		recorder.start();
	}

	public void stopRecording(View view) {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		recorder.stop();
		recorder.release();
		addRecordingToMediaLibrary();
	}

	protected void addRecordingToMediaLibrary() {
		ContentValues values = new ContentValues(4);
		long current = System.currentTimeMillis();
		values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
		values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
		values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
		values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
		ContentResolver contentResolver = getContentResolver();

		Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		newUri = contentResolver.insert(base, values);

		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
		Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
	}

	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			// songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			// songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utils.getProgressPercentage(currentDuration,
					totalDuration));
			// Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(
				songProgressBar.getProgress(), totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

}
