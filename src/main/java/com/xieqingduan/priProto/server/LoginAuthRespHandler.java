package com.xieqingduan.priProto.server;

import com.xieqingduan.priProto.MessageType;
import com.xieqingduan.priProto.struct.Header;
import com.xieqingduan.priProto.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//服务端握手认证  注意： 检查是否重复登陆   是否允许登陆
public class LoginAuthRespHandler extends ChannelHandlerAdapter {

    private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<String,Boolean>();//在线客户端
    private String[] whitekList = new String []{"127.0.0.1"};//白名单模式



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if(message.getHeader()!=null && message.getHeader().getType() == MessageType.LOGIN_REQ.value()){
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            if (nodeCheck.containsKey(nodeIndex)){ //该客户端已经登陆过一次
                loginResp = buildResponse((byte)-1);
            }else{//该客户端第一次登陆
                //判断客户端是否为白名单
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOk = false;
                for(String WIP:whitekList){
                    if (WIP.equals(ip)){
                        isOk = true;
                        break;
                    }
                }
                loginResp = isOk?buildResponse((byte)0):buildResponse((byte)-1);
                if (isOk){
                    nodeCheck.put(nodeIndex,true);
                }
            }
            System.out.println("The login response is :"+loginResp+"  body ["+loginResp.getBody()+"]");
            ctx.writeAndFlush(loginResp);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildResponse(byte b) {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(b);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }

}
