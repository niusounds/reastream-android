package com.eje_c.libreastream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReaStreamReceiver implements AutoCloseable {

    public static final int DEFAULT_PORT = 58710;
    public static final String DEFAULT_IDENTIFIER = "default";
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final ReaStreamPacket audioPacket = new ReaStreamPacket();
    private final ByteBuffer buffer;
    private String identifier = DEFAULT_IDENTIFIER;

    /**
     * Create ReaStream receiver with UDP socket default port.
     *
     * @throws SocketException
     */
    public ReaStreamReceiver() throws SocketException {
        this(DEFAULT_PORT);
    }

    /**
     * Create ReaStream receiver with UDP socket specific port.
     *
     * @param port UDP port
     * @throws SocketException
     */
    public ReaStreamReceiver(int port) throws SocketException {
        this(new DatagramSocket(port));
    }

    /**
     * Create ReaStream receiver with UDP socket specific port.
     *
     * @param socket Pre-created socket
     */
    public ReaStreamReceiver(DatagramSocket socket) {
        buffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.PACKET_HEADER_BYTE_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        packet = new DatagramPacket(buffer.array(), buffer.capacity());
        this.socket = socket;
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
