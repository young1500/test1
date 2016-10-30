package com.hawk.funday.ui.fragment.video;

import android.app.Activity;
import android.app.Fragment;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.aisen.android.common.utils.Logger;

/**
 * Created by wangdan on 16/10/20.
 */
public class YoutubeFragment extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "YoutubeFragment";

    private YouTubePlayer mPlayer;

    private String videoId;

    public static YoutubeFragment getFragment(Activity context) {
        Fragment fragment = context.getFragmentManager().findFragmentByTag("YoutubeFragment");

        if (fragment != null && fragment instanceof YoutubeFragment) {
            return (YoutubeFragment) fragment;
        }

        return null;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            Logger.d(TAG, "onInitializationSuccess, videoId = " + videoId);

            youTubePlayer.loadVideo(videoId);

            mPlayer = youTubePlayer;
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Logger.d(TAG, "onInitializationFailure");
    }

    public void play(String videoId) {
        if (mPlayer != null) {
            mPlayer.release();
        }

        this.videoId = videoId;

        initialize("AIzaSyBrXT-ptgetdaTmRG02fAVkSytH-MI18bI", this);
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

}
