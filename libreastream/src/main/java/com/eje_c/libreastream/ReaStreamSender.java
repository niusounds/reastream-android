package com.eje_c.libreastream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ReaStreamSender implements AutoCloseable {

    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private ByteBuffer buffer;
    private ReaStreamPacket audioPacket = new ReaStreamPacket();

    public ReaStreamSender() throws SocketException {
        this(new DatagramSocket());
    }

    /**
     * @param socket Pre-created socket
     */
    public ReaStreamSender(DatagramSocket socket) {

        this.socket = socket;
        packet = new DatagramPacket(new byte[0], 0);

        setPort(ReaStream.DEFAULT_PORT);
        setIdentifier(ReaStream.DEFAULT_IDENTIFIER);
    }

    /**
     * Send ReaStream audio packet.
     *
     * @param audioData Audio data
     * @param readCount Must be less than or equal to audioData.length
     * @throws IOException
     */
    public void send(float[] audioData, int readCount) throws IOException {

        audioPacket.setAudioData(audioData, readCount);

        // Create buffer
        if (!audioPacket.isCapableBuffer(buffer)) {
            buffer = audioPacket.createCapableBuffer();
            packet.setData(buffer.array());
        }

        audioPacket.writeToBuffer(buffer);

        packet.setLength(audioPacket.packetSize);
        socket.send(packet);
    }

    public void setIdentifier(String identifier) {
        audioPacket.setIdentifier(identifier);
    }

    public String getIdentifier() {
        return audioPacket.getIdentifier();
    }

    public void setSampleRate(int sampleRate) {
        audioPacket.sampleRate = sampleRate;
    }

    public int getSampleRate() {
        return audioPacket.sampleRate;
    }

    public void setChannels(byte channels) {
        audioPacket.channels = channels;
    }

    public byte getChannels() {
        return audioPacket.channels;
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        packet.setAddress(remoteAddress);
    }

    public InetAddress getRemoteAddress() {
        return packet.getAddress();
    }

    public void setPort(int port) {
        packet.setPort(port);
    }

    public int getPort() {
        return packet.getPort();
    }

    /**
     * Close UDP socket.
     */
    @Override
    public void close() {
        socket.close();
    }
}
