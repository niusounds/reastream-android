package com.eje_c.libreastream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.FloatBuffer;

public class AudioRecordSrc implements AutoCloseable {
    private final AudioRecord record;
    private final float[] recordBuffer;
    private final FloatBuffer floatBuffer;
    private boolean recording;

    public AudioRecordSrc(int sampleRate) {
        final int channel = AudioFormat.CHANNEL_IN_MONO;
        final int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, bufferSize);
        floatBuffer = FloatBuffer.allocate(bufferSize / 4);
        recordBuffer = floatBuffer.array();
    }

    public void start() {

        if (!recording) {
            record.startRecording();
            recording = true;
        }
    }

    public void stop() {

        if (recording) {
            recording = false;
            record.stop();
        }
    }

    @Override
    public void close() {
        if (record != null) {
            record.release();
        }
    }


    public FloatBuffer read() {

        int readCount = record.read(recordBuffer, 0, recordBuffer.length, AudioRecord.READ_NON_BLOCKING);
        floatBuffer.limit(readCount);

        return floatBuffer;
    }
}
