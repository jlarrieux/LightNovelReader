package com.example.ttsexample;

import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

public class TtsUtteranceListener extends UtteranceProgressListener {
    private static String ID = "TtsUtteranceListener";

    @Override
    public void onStart(String utteranceId) {
        Log.i(ID, "utterance start:" + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.i(ID, "utterance done:" + utteranceId);
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        Log.i(ID, "utterance STOP:" + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.i(ID, "utterance error:" + utteranceId);
    }
}
