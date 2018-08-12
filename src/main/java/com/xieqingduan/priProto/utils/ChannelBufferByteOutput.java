package com.xieqingduan.priProto.utils;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteOutput;

import java.io.IOException;

public class ChannelBufferByteOutput implements ByteOutput {

    private final ByteBuf buffer;

    /**
     * Create a new instance which use the given {@link ByteBuf}
     */
    public ChannelBufferByteOutput(ByteBuf buffer) {
        this.buffer = buffer;
    }



    /**
     * Return the {@link ByteBuf} which contains the written content
     */
    ByteBuf getBuffer() {
        return buffer;
    }
    
    public void close() throws IOException {

    }


    public void flush() throws IOException {

    }

    public void write(int b) throws IOException {
        buffer.writeByte(b);
    }

    public void write(byte[] bytes) throws IOException {
        buffer.writeBytes(bytes);
    }

    public void write(byte[] bytes, int srcIndex, int length) throws IOException {
        buffer.writeBytes(bytes, srcIndex, length);
    }

}