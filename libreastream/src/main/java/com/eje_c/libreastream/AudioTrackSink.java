package com.eje_c.libreastream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioTrackSink implements AutoCloseable, ReaStreamReceiverService.OnReaStreamPacketListener {

    private static final int FLOAT_BYTE_SIZE = Float.SIZE / Byte.SIZE;
    private final AudioTrack track;
    private final float[] interleavedSamples = new float[ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES];

    public AudioTrackSink(int sampleRate) {
        int bufferSize = Math.max(
                AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT),
                ReaStreamPacket.MAX_BLOCK_LENGTH * FLOAT_BYTE_SIZE
        );
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT, bufferSize, AudioTrack.MODE_STREAM);
    }

    /**
     * Must call this before first {@link #onReceive(ReaStreamPacket)}.
     */
    public void start() {

        if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            track.play();
        }
    }

    public void stop() {

        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            track.stop();
        }
    }

    @Override
    public void close() {
        if (track != null) {
            track.release();
        }
    }

    @Override
    public void onReceive(ReaStreamPacket packet) {
        if (packet.isAudioData()) {
            packet.getInterleavedAudioData(interleavedSamples);
            track.write(interleavedSamples, 0, packet.blockLength / ReaStreamPacket.PER_SAMPLE_BYTES, AudioTrack.WRITE_NON_BLOCKING);
        }
    }
}
