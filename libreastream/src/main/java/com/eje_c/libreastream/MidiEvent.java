package com.eje_c.libreastream;

import java.util.Arrays;

public class MidiEvent {
    public static final int BYTE_SIZE = 4 + 4 + 4 + 4 + 4 + 4 + 3 + 1 + 1 + 1 + 2;
    public int type;
    public int byteSize;
    public int sampleFramesSinceLastEvet;
    public int flags;
    public int noteLength;
    public int noteOffset;
    public byte[] midiData = new byte[3];
    public byte detune;
    public byte noteOffVelocity;

    @Override
    public String toString() {
        return "MidiEvent{" +
                "type=" + type +
                ", byteSize=" + byteSize +
                ", sampleFramesSinceLastEvet=" + sampleFramesSinceLastEvet +
                ", flags=" + flags +
                ", noteLength=" + noteLength +
                ", noteOffset=" + noteOffset +
                ", midiData=" + Arrays.toString(midiData) +
                ", detune=" + detune +
                ", noteOffVelocity=" + noteOffVelocity +
                '}';
    }
}
