/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.mobly.snippet.bundled;

import android.content.Context;
import android.content.ComponentName;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.mobly.snippet.bundled.utils.JsonDeserializer;
import com.google.android.mobly.snippet.bundled.utils.JsonSerializer;
import com.google.android.mobly.snippet.Snippet;
import com.google.android.mobly.snippet.rpc.Rpc;
import com.google.android.mobly.snippet.util.Log;

import java.io.IOException;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener;
import android.media.session.PlaybackState;
import android.media.session.MediaSession.Callback;
import android.support.test.InstrumentationRegistry;
import android.view.KeyEvent;
import java.util.List;

import android.app.NotificationManager;
import android.service.notification.NotificationListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* Snippet class to control media playback. */
public class MediaSnippet implements Snippet {
    private final MediaPlayer mPlayer;
    private static final String TAG = "MediaSnippet";
    private static final boolean VDBG = true;
    //private final MediaSession mSession;
    private Handler mHandler;
    private final MediaSessionManager mSessionManager;
    private final Context mContext;
    private MediaSessionManager.OnActiveSessionsChangedListener mSessionListener;
    private MediaController.Callback mMediaCtrlCallback = null;
    private String CUSTOM_ACTION_GET_PLAY_STATUS_NATIVE =
        "com.android.bluetooth.avrcpcontroller.CUSTOM_ACTION_GET_PLAY_STATUS_NATIVE";
    private final JsonSerializer mJsonSerializer = new JsonSerializer();
    public MediaSnippet() {
        mPlayer = new MediaPlayer();
        mHandler = new Handler(Looper.getMainLooper());
        mContext = InstrumentationRegistry.getContext();
        mSessionManager = (MediaSessionManager) mContext.getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSessionListener = new SessionChangeListener();
        if (mSessionManager != null) {
            //ComponentName compName =
            //        new ComponentName(mContext.getPackageName(), this.getClass().getName());
            //mSessionManager.addOnActiveSessionsChangedListener(mSessionListener, null,
            //        mHandler);
            if (VDBG) {
                //List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
                //List<MediaController> mcl = mSessionManager.getActiveSessions(null);
                //List<MediaController> mcl = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
                //Log.d(TAG + " Num Sessions " + controllers.size());
                //for (int i = 0; i < mcl.size(); i++) {
                //}
            }
        }
        //mMediaCtrlCallback = new MediaControllerCallback();
    }
/*
    public class NotificationListener extends NotificationListenerService {
        public NotificationListener() {
        }
    }
*/
     /**
     * The listener that was setup for listening to changes to Active Media Sessions.
     * This listener is useful in both Car and Phone sides.
     */
    private class SessionChangeListener
            implements MediaSessionManager.OnActiveSessionsChangedListener {
        /**
         * On the Phone side, it listens to the BluetoothSL4AAudioSrcMBS (that the SL4A app runs)
         * becoming active.
         * On the Car side, it listens to the A2dpMediaBrowserService (associated with the
         * Bluetooth Audio App) becoming active.
         * The idea is to get a handle to the MediaController appropriate for the device, so
         * that we can send and receive Media commands.
         */
        @Override
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            if (VDBG) {
                for (int i = 0; i < controllers.size(); i++) {
                    //MediaController controller = controllers.get(i);
                    Log.d(TAG + "Active session : " + i + ((controllers.get(
                            i))).getPackageName());
                }
            }
/*
            for (int i = 0; i < controllers.size(); i++) {
                MediaController controller = (MediaController) controllers.get(i);
                if ((controller.getTag().contains(BluetoothSL4AAudioSrcMBS.getTag()))
                        || (controller.getTag().contains(A2DP_MBS_TAG))) {
                    setCurrentMediaController(controller);
                    return;
                }
            }
*/
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            Log.d(TAG + " onPlaybackStateChanged: " + state.getState());
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            Log.d(TAG + " onMetadataChanged ");
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            Log.d(TAG +  "onSessionEvent : " + event);
        }
    }

    @Rpc(description = "Resets snippet media player to an idle state, regardless of current state.")
    public void mediaReset() {
        mPlayer.reset();
    }

    @Rpc(description = "Play an audio file stored at a specified file path in external storage.")
    public void mediaPlayAudioFile(String mediaFilePath) throws IOException {
        mediaReset();
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mPlayer.setDataSource(mediaFilePath);
        mPlayer.prepare(); // Synchronous call blocks until the player is ready for playback.
        mPlayer.start();
    }

    @Rpc(description = "Stops media playback.")
    public void mediaStop() throws IOException {
        mPlayer.stop();
    }

    @Rpc(description = "Pause media playback.")
    public void mediaPause() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().pause();
        //mPlayer.pause();
        for (int i = 0; i < controllers.size(); i++) {
                    //MediaController controller = controllers.get(i);
            Log.d(TAG + "Package names : " + i + ((controllers.get(
                i))).getPackageName());
            mController = controllers.get(i);
            mController.getTransportControls().pause();
            PlaybackState pbStatus = mController.getPlaybackState();
            if (pbStatus == null) {
                Log.d(TAG + "No pbstatus available!!!");
                return;
            }
            Log.d(TAG + "pbStatus : " + pbStatus);
        }
    }

    @Rpc(description = "Play media playback.")
    public void mediaPlay() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().play();
    }

    @Rpc(description = "media meta")
    //public void mediaMeta() throws IOException {
    public JSONObject mediaMeta() throws JSONException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        MediaMetadata metadata = mController.getMetadata();
        if (metadata == null) {
            Log.d(TAG + "No metadata available!!!");
            return null;
        }
        Log.d(TAG + "Title: " + metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        Log.d(TAG + "Album: " + metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
        return mJsonSerializer.toJson(metadata);
    }

    @Rpc(description = "check playback status")
    public JSONObject mediaStatus() throws JSONException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        PlaybackState pbStatus = null;
/*
        MediaController mController = controllers.get(0);
        PlaybackState pbStatus = mController.getPlaybackState();
        if (pbStatus == null) {
            Log.d(TAG + "No pbstatus available!!!");
            return null;
        }
*/
        for (int i = 0; i < controllers.size(); i++) {
                    //MediaController controller = controllers.get(i);
            Log.d(TAG + "Package names : " + i + ((controllers.get(
                i))).getPackageName());
            MediaController mController = controllers.get(i);
            pbStatus = mController.getPlaybackState();
            if (pbStatus == null) {
                Log.d(TAG + "No pbstatus available!!!");
                return null;
            }
            Log.d(TAG + "pbStatus : " + pbStatus);
        }
        return mJsonSerializer.toJson(pbStatus);
    }

    @Rpc(description = "Play next song.")
    public void mediaNext() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().skipToNext();
    }

    @Rpc(description = "Play previous song.")
    public void mediaPrev() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().skipToPrevious();
    }

    @Rpc(description = "Play Fast Forward song.")
    public void mediaFwd() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().fastForward();
    }

    @Rpc(description = "Play Rewind song.")
    public void mediaRwd() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        MediaController mController = controllers.get(0);
        mController.getTransportControls().rewind();
    }

    @Rpc(description = "Send Custom Get Playback Status.")
    public void mediaGetPlayStatus() throws IOException {
        List<MediaController> controllers = mSessionManager.getActiveSessions(new ComponentName(mContext, NotificationListener.class));
        Log.d(TAG + "found " + controllers.size() + " controllers");
        for (int i = 0; i < controllers.size(); i++) {
            Log.d(TAG + "Package names : " + i + ((controllers.get(
                i))).getPackageName());
            MediaController mController = controllers.get(i);
            mController.getTransportControls().sendCustomAction(CUSTOM_ACTION_GET_PLAY_STATUS_NATIVE, null);
        }
    }

    @Override
    public void shutdown() {}
}
