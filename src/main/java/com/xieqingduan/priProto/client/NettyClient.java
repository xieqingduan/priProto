package com.xieqingduan.priProto.client;

import com.xieqingduan.priProto.utils.NettyMessageDecoder;
import com.xieqingduan.priProto.utils.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    EventLoopGroup  group = new NioEventLoopGroup();

    public void connect(final String host, final int post) throws InterruptedException {
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            /**
                             * maxFrameLength：表示的是包的最大长度，超出包的最大长度netty将会做一些特殊处理，比如丢弃；
                             * lengthFieldOffset：指的是长度域的偏移量，表示跳过指定长度个字节之后的才是长度域，Header-crcCode int占四个字节；
                             * lengthFieldLength：记录该帧数据长度的字段本身的长度，Header-length int占四个字节；
                             * lengthAdjustment：该字段加长度字段等于数据帧的长度，包体长度调整的大小，长度域的数值表示的长度加上这个修正值表示的就是带header的包；
                             * 这里lengthAdjustment 为-8的意思为 header我们需要读取crcCode、length共8个字节，所以修正值为-8，否者buf从长度值后开始
                             */
                             p.addLast(new NettyMessageDecoder(1024*1024,4,4,-8,0));
                             p.addLast(new NettyMessageEncoder());
                             p.addLast(new ReadTimeoutHandler(50));
                             p.addLast(new LoginAuthReqHandler());
                             p.addLast(new HeartBeatReqHandler());

                        }
                    });
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, post)).sync();
            future.channel().closeFuture().sync();
        }finally {
            //连接断开后每隔五秒重连接
            executor.execute(new Runnable() {
                public void run() {
                    try{
                        TimeUnit.SECONDS.sleep(5);
                        connect(host, post);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyClient().connect("127.0.0.1",9999);
    }

}
