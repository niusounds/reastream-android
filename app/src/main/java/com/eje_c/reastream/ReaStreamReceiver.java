package com.eje_c.reastream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReaStreamReceiver implements AutoCloseable {

    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final ReaStreamPacket audioPacket = new ReaStreamPacket();
    private final ByteBuffer buffer;
    private String identifier = ReaStream.DEFAULT_IDENTIFIER;

    /**
     * Create ReaStream receiver with UDP socket default port.
     *
     * @throws SocketException
     */
    public ReaStreamReceiver() throws SocketException {
        this(ReaStream.DEFAULT_PORT);
    }

    /**
     * Create ReaStream receiver with UDP socket specific port.
     *
     * @param port UDP port
     * @throws SocketException
     */
    public ReaStreamReceiver(int port) throws SocketException {

        buffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.PACKET_HEADER_BYTE_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        socket = new DatagramSocket(port);
        packet = new DatagramPacket(buffer.array(), buffer.capacity());
    }

    /**
     * @param identifier Identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return Identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Wait for receiving {@link ReaStreamPacket}.
     * This method blocks until receiving ReaStream packet with same identifier.
     *
     * @return Received ReaStream packet.
     * @throws IOException
     */
    public ReaStreamPacket receive() throws IOException {

        do {
            socket.receive(packet);
            audioPacket.readFromBuffer(buffer);
        } while (!identifier.equals(audioPacket.getIdentifier()));

        return audioPacket;
    }

    /**
     * Close UDP socket.
     */
    @Override
    public void close() {
        socket.close();
    }
}
