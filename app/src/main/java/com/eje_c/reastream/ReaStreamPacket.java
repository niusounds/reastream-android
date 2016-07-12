package com.eje_c.reastream;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents ReaStream packet data.
 */
public class ReaStreamPacket {

    public static final short MAX_BLOCK_LENGTH = 1200;
    public static final int PER_SAMPLE_BYTES = Float.SIZE / Byte.SIZE;
    public static final int PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32 + 1 + 4 + 2;

    public int packetSize;                         // 4 bytes: int packetsize: 4+4+32+1+4+2+sblocklen
    public final byte[] identifier = new byte[32]; // 32 bytes: identifier string (zero padded, last byte always 0)
    public byte channels = 1;                      // 1 byte: char nch [1-64]
    public int sampleRate = 44100;                 // 4 bytes: int samplerate
    public short blockLength;                      // 2 bytes: short sblocklen -- largest supported is 1200 (larger blocks are separated)
    public float[] audioData;                      // sblocklen bytes: sample data (32 bit floats, non-interleaved)

    /**
     * Set identifier with String.
     *
     * @param identifier Must be less than or equal to 32 bytes.
     */
    public void setIdentifier(String identifier) {
        byte[] bytes = identifier.getBytes();
        System.arraycopy(bytes, 0, this.identifier, 0, bytes.length);
    }

    /**
     * Get identifier as String.
     *
     * @return identifier
     */
    public String getIdentifier() {
        return new String(identifier).trim();
    }

    /**
     * Read data from {@link ByteBuffer} which is read from UDP socket.
     *
     * @param buffer Buffer from received UDP raw packet.
     * @return {@code true} if buffer is ReaStream packet and have read, otherwise {@code false}.
     */
    public boolean readFromBuffer(@NonNull ByteBuffer buffer) {
        buffer.position(0);

        if (buffer.get() == (byte) 77// M
                && buffer.get() == (byte) 82 // R
                && buffer.get() == (byte) 83 // S
                && buffer.get() == (byte) 82 // R
                ) {

            packetSize = buffer.getInt();
            buffer.get(identifier);
            channels = buffer.get();
            sampleRate = buffer.getInt();
            blockLength = buffer.getShort();

            final int sizeInFloats = blockLength / PER_SAMPLE_BYTES;
            if (audioData == null || audioData.length < sizeInFloats) {
                audioData = new float[sizeInFloats];
            }

            for (int i = 0; i < sizeInFloats; i++) {
                audioData[i] = buffer.getFloat();
            }

            return true;
        }

        return false;
    }

    /**
     * Write data to {@link ByteBuffer} which will be sent to UDP socket.
     *
     * @param buffer
     */
    public void writeToBuffer(@NonNull ByteBuffer buffer) {

        buffer.position(0);

        buffer.put((byte) 77); // M
        buffer.put((byte) 82); // R
        buffer.put((byte) 83); // S
        buffer.put((byte) 82); // R
        buffer.putInt(packetSize);
        buffer.put(identifier);
        buffer.put(channels); // ch
        buffer.putInt(sampleRate);
        buffer.putShort(blockLength);

        for (int i = 0; i < blockLength / PER_SAMPLE_BYTES; i++) {
            buffer.putFloat(audioData[i]);
        }
    }

    /**
     * Set audio data.
     * Usually, audio data is read from {@link android.media.AudioRecord#read(float[], int, int, int)}.
     * Currently supported mono audioData only.
     *
     * @param audioData   Audio data.
     * @param sampleCount Valid audio data sample count. Must be less than or equal to {@code audioData.length}.
     */
    public void setAudioData(@NonNull float[] audioData, int sampleCount) {

        // TODO 2 or above channels support
        this.blockLength = (short) (sampleCount * PER_SAMPLE_BYTES);
        this.packetSize = PACKET_HEADER_BYTE_SIZE + blockLength;
        this.audioData = audioData;
    }

    /**
     * @param buffer
     * @return {@code true} if buffer can be passed to {@link #writeToBuffer(ByteBuffer)}, otherwise {@code false}.
     */
    public boolean isCapableBuffer(@Nullable ByteBuffer buffer) {
        return buffer != null && buffer.capacity() >= packetSize;
    }

    /**
     * Create buffer which can be passed to {@link #writeToBuffer(ByteBuffer)}.
     *
     * @return created buffer.
     */
    public ByteBuffer createCapableBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(packetSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    /**
     * {@link #audioData} is not interleaved.
     * This method puts interleaved audio data to {@code outInterleavedAudioData}.
     *
     * @param outInterleavedAudioData Output audio data.
     */
    public void getInterleavedAudioData(float[] outInterleavedAudioData) {

        int samples = blockLength / ReaStreamPacket.PER_SAMPLE_BYTES;
        int samplesPerChannel = samples / channels;

        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < channels; ch++) {
                outInterleavedAudioData[i * channels + ch] = audioData[samplesPerChannel * ch + i];
            }
        }
    }
}
