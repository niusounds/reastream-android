package com.eje_c.libreastream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioTrackSink implements AutoCloseable {

    private static final int FLOAT_BYTE_SIZE = Float.SIZE / Byte.SIZE;
    private final AudioTrack track;
    private final float[] interleavedSamples = new float[ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES];
    private boolean started;

    public AudioTrackSink(int sampleRate) {
        int bufferSize = Math.max(
                AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT),
                ReaStreamPacket.MAX_BLOCK_LENGTH * FLOAT_BYTE_SIZE
        );
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT, bufferSize, AudioTrack.MODE_STREAM);
    }

    public void start() {

        if (!started) {
            track.play();
            started = true;
        }
    }

    public void stop() {

        if (started) {
            started = false;
            track.stop();
        }
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void close() {
        if (track != null) {
            track.release();
        }
    }

    public void onReceivePacket(ReaStreamPacket audioPacket) {
        audioPacket.getInterleavedAudioData(interleavedSamples);
        track.write(interleavedSamples, 0, audioPacket.blockLength / ReaStreamPacket.PER_SAMPLE_BYTES, AudioTrack.WRITE_NON_BLOCKING);
    }
}
