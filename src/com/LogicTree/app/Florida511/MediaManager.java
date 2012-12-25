// /////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Costas Kleopa.
// All Rights Reserved.
// 
// Costas Kleopa, costas.kleopa@gmail.com
//
// This source code is the confidential property of Costas Kleopa.
// All proprietary rights, including but not limited to any trade
// secrets, copyright, patent or trademark rights in and to this source
// code are the property of Costas Kleopa. This source code is not to
// be used, disclosed or reproduced in any form without the express
// written consent of Costas Kleopa.
// /////////////////////////////////////////////////////////////////////
package com.LogicTree.app.Florida511;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/**
 * @author costas
 *
 */
public class MediaManager extends Service 
	implements OnPreparedListener, OnErrorListener, OnCompletionListener, OnAudioFocusChangeListener   {
	
	private static final int IDLE 	 = 0;
	private static final int PREPARE = 1;
	private static final int PLAY	 = 2;

	public static MediaManager manager = null;
	
	private String audioPrompts[] = null;
	
	private int currentState  = IDLE;

	private int promptIndex;

	private MediaPlayer mMediaPlayer;

	private Context mContext;
	/**
	 * @return
	 */
	public static MediaManager getInstance(Context context) {
		if (manager == null) {
			manager = new MediaManager(context);
		}
		return manager;
	}
	
	/**
	 * 
	 */
	private MediaManager(Context context) {
		promptIndex 	= 0;
		currentState  	= IDLE;
		audioPrompts  	= null;
		mContext 		= context;
		//resetMediaPlayer();
		/*
		mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
		*/
		//mMediaPlayer.setAudioStreamType(AudioManager.);
	}
	/**
	 * @author costas
	 *
	 */
	public class AudioPlayTask extends AsyncTask<String, Integer, Long> {
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		private MediaManager manager;
		/**
		 * @param activity
		 */
		public AudioPlayTask(MediaManager manager) {
			this.manager = manager;
		}
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(String... prompts) {
			playAudioFiles(prompts);
			return new Long(0);
		}
		
		@Override
		protected void onPostExecute(Long result) {
			
			super.onPostExecute(result);
		}
	}

	public void playAudioFiles(String... prompts) {
		if (currentState != IDLE) {
			stopPlayingFiles();
		} 
		
		audioPrompts = prompts;
		promptIndex  = 0;

		if (audioPrompts.length > 0) {
			try {
				StringBuffer filename = new StringBuffer();
				AudioDownloader adl = new AudioDownloader(mContext.getCacheDir());
				for (int i = promptIndex; i < audioPrompts.length; i++) {
					filename 	 = new StringBuffer();
					boolean succ = adl.download(audioPrompts[i], filename);
					if (true) {
						if (i == promptIndex) {
							currentState = PREPARE;
							resetMediaPlayer();
							mMediaPlayer.setDataSource(filename.toString());
							mMediaPlayer.prepare();
						}
					} else {
						promptIndex++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
    private void stopPlayingFiles() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		currentState = IDLE;
		promptIndex  = 0;
	}

	/** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        currentState = PLAY;
    }

	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("audio", "error on playback: "+ what + ", "+ extra);
		
		return false;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCompletion(MediaPlayer mp) {
		mMediaPlayer.release();

		if (currentState == IDLE) {
			return;
		}
		
		promptIndex++;

		if (promptIndex < audioPrompts.length) {	
			try {
				StringBuffer filename = new StringBuffer();
				AudioDownloader adl = new AudioDownloader(mContext.getCacheDir());
				adl.download(audioPrompts[promptIndex], filename);
				if (filename.length() != 0) {
					resetMediaPlayer();
					mMediaPlayer.setDataSource(filename.toString());
					mMediaPlayer.prepare();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			
		}
		
	}

	/**
	 * 
	 */
	private void resetMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnCompletionListener(this);
	}

	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			//if (mMediaPlayer == null) initMediaPlayer();
			//else 
			if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
			mMediaPlayer.setVolume(1.0f, 1.0f);
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and release media player
			if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
			break;
		}		
	}


	@Override
	public void onDestroy() {
       if (mMediaPlayer != null) mMediaPlayer.release();
    }

	/**
	 * @param prompts
	 */
	public void playAudioFilesInBackground(ArrayList<String> prompts) {
		String audioURLs[] = new String[prompts.size()];
		prompts.toArray(audioURLs);
		AudioPlayTask audio = new AudioPlayTask(this);
		audio.execute(audioURLs);
	}
}
