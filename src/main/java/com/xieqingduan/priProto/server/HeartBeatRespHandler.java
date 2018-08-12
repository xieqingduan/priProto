package com.xieqingduan.priProto.server;

import com.xieqingduan.priProto.MessageType;
import com.xieqingduan.priProto.struct.Header;
import com.xieqingduan.priProto.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatRespHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if(message.getHeader()!=null && message.getHeader().getType() == MessageType.HEARTBEAT_REQ.value()){
            //客户端发送的心跳检测
            System.out.println("Receive client heart beat message : ---> "+message);
            NettyMessage heartBeat = buildHeatBeat();
            System.out.println("Send heart beat response message to client : ---> " +heartBeat);
            ctx.writeAndFlush(heartBeat);
        }else{
            ctx.writeAndFlush(msg);
        }
    }

    private NettyMessage buildHeatBeat() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP.value());
        message.setHeader(header);
        return message;
    }

}
