package com.xieqingduan.priProto.utils;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.Unmarshaller;

import java.io.IOException;

//消息解码工具类
public class MarshallingDecoder {
    private final Unmarshaller unmarshaller;
    public MarshallingDecoder() throws IOException {
        this.unmarshaller = MarshallingCodecFactory.buildUnMarshalling();
    }

    public  Object decode(ByteBuf in) throws Exception {
        int objectSize = in.readInt();
        ByteBuf buf = in.slice();
        ChannelBufferByteInput input = new ChannelBufferByteInput(buf);
        try{
            unmarshaller.start(input);
            Object obj = unmarshaller.readObject();
            unmarshaller.finish();
            in.readerIndex(in.readerIndex()+objectSize);
            return obj;
        }finally {
            unmarshaller.close();
        }
    }
}
