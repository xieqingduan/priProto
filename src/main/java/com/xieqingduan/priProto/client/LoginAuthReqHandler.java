package com.xieqingduan.priProto.client;

import com.xieqingduan.priProto.MessageType;
import com.xieqingduan.priProto.struct.Header;
import com.xieqingduan.priProto.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;


//客户端 握手认证
public class LoginAuthReqHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端连上服务端后 向服务端发送握手认证
        NettyMessage message = buildLoginReq();
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage)msg;
        //如果是握手应答消息，需要判断是否认证成功
        if(message.getHeader()!=null && message.getHeader().getType() == MessageType.LOGIN_RESP.value()){
            byte longResult =  (Byte) message.getBody();
            if (longResult !=(byte) 0 ){
                ctx.close();//关闭连接
            }else{
                System.out.println("Login is ok : " + message);
                ctx.fireChannelRead(msg);
            }
        }else{
            ctx.fireChannelRead(msg);//消息继续往链传递下去
        }
    }

    private NettyMessage buildLoginReq() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_REQ.value());
        message.setHeader(header);
        return message;
    }


}
