package com.xieqingduan.priProto.client;

import com.xieqingduan.priProto.MessageType;
import com.xieqingduan.priProto.struct.Header;
import com.xieqingduan.priProto.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

//客户端心跳检测  检测链路可用性
public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;//volatile 并发编程可用性



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if(message.getHeader()!=null && message.getHeader().getType()==MessageType.LOGIN_RESP.value()){
            //客户端与服务端握手成功
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx),0,5000,TimeUnit.MILLISECONDS);
        }else if(message.getHeader()!=null && message.getHeader().getType()== MessageType.HEARTBEAT_RESP.value()){
            //客户端收到服务端应答
            System.out.println("Client receive server heart beat message : ---> "+ message);
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    private class HeartBeatTask implements Runnable{

        private final ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx){
            this.ctx = ctx;
        }

        public void run() {
            NettyMessage message = buildHeatBeat();
            System.out.println("Client send heart beat message to server : ---> " + message);
            ctx.writeAndFlush(message);
        }

        private NettyMessage buildHeatBeat() {
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            header.setType(MessageType.HEARTBEAT_REQ.value());
            message.setHeader(header);
            return message;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(heartBeat!=null){
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

}
