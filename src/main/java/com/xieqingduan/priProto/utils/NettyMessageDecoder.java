package com.xieqingduan.priProto.utils;

import com.xieqingduan.priProto.struct.Header;
import com.xieqingduan.priProto.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.util.HashMap;

public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {
    MarshallingDecoder marshallingDecoder;

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) throws IOException {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
        marshallingDecoder = new MarshallingDecoder();
    }


    @Override
    public  Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ByteBuf in = (ByteBuf)super.decode(ctx, buf);
        if(in==null){
            return null;
        }
        //buf解码成NettyMessage
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setCrcCode(in.readInt());
        header.setLength(in.readInt());
        header.setSessionID(in.readLong());
        header.setType(in.readByte());
        header.setPriority(in.readByte());
        int size = in.readInt();
        if(size>0){
            HashMap<String, Object> attch = new HashMap<String, Object>();
            int keySize = 0;
            byte[] keyArray = null;
            String key = null;
            for(int i = 0;i<size;i++){
                keySize = in.readInt();
                keyArray = new byte[keySize];
                in.readBytes(keyArray);
                key = new String(keyArray, "UTF-8");
                attch.put(key,marshallingDecoder.decode(in));
            }
            keyArray = null;
            key =null;
            header.setAttachment(attch);
        }
        if(in.readableBytes()>4){
            message.setBody(marshallingDecoder.decode(in));
        }
        message.setHeader(header);
        return message;
    }
}
