package com.eje_c.libreastream;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReaStream implements AutoCloseable {
    public static final int DEFAULT_PORT = 58710;
    public static final String DEFAULT_IDENTIFIER = "default";
    private static final int FLOAT_BYTE_SIZE = Float.SIZE / Byte.SIZE;
    private AudioRecord record;
    private boolean recording;
    private boolean playing;
    private int sampleRate = 44100;
    private int bufferSize;
    private ReaStreamSender sender;     // Non null while sending
    private ReaStreamReceiver receiver; // Non null while receiving
    private boolean enabled = true;
    private InetAddress remoteAddress;

    public void startSending() {

        if (record == null) {
            final int channel = AudioFormat.CHANNEL_IN_MONO;
            final int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat);
            record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, bufferSize);
        }

        if (record.getState() == AudioRecord.STATE_INITIALIZED) {
            record.startRecording();
            recording = true;
            new SenderThread().start();
        }
    }

    public void stopSending() {
        recording = false;
        close();
    }

    public void startReveiving() {

        if (!playing) {
            playing = true;
            new ReceiverThread().start();
        }
    }

    public void stopReceiving() {
        playing = false;
    }

    @Override
    public void close() {
        if (record != null) {
            record.release();
            record = null;
        }
    }

    public boolean isSending() {
        return recording;
    }

    public boolean isReceiving() {
        return playing;
    }

    public void setIdentifier(String identifier) {

        if (sender != null) {
            sender.setIdentifier(identifier);
        }

        if (receiver != null) {
            receiver.setIdentifier(identifier);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRemoteAddress(String remoteAddress) throws UnknownHostException {
        setRemoteAddress(InetAddress.getByName(remoteAddress));
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;

        if (sender != null) {
            sender.setRemoteAddress(remoteAddress);
        }
    }

    private class SenderThread extends Thread {

        @SuppressLint("NewApi")
        @Override
        public void run() {

            try (ReaStreamSender sender = new ReaStreamSender()) {
                ReaStream.this.sender = sender;

                sender.setSampleRate(sampleRate);
                sender.setChannels((byte) 1);
                sender.setRemoteAddress(remoteAddress);

                float[] recordBuffer = new float[bufferSize / FLOAT_BYTE_SIZE];

                while (recording) {

                    // Read from mic and send it
                    int readCount = record.read(recordBuffer, 0, recordBuffer.length, AudioRecord.READ_NON_BLOCKING);
                    if (enabled && readCount > 0) {
                        sender.send(recordBuffer, readCount);
                    }
                }

                ReaStream.this.sender = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiverThread extends Thread {

        @SuppressLint("NewApi")
        @Override
        public void run() {

            try (ReaStreamReceiver receiver = new ReaStreamReceiver();
                 AudioTrackSink audioTrackSink = new AudioTrackSink(sampleRate)) {
                audioTrackSink.start();

                ReaStream.this.receiver = receiver;

                while (playing) {
                    if (enabled) {
                        ReaStreamPacket audioPacket = receiver.receive();
                        audioTrackSink.onReceivePacket(audioPacket);
                    }
                }

                ReaStream.this.receiver = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
