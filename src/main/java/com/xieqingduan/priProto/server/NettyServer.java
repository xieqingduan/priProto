package com.xieqingduan.priProto.server;
import com.xieqingduan.priProto.utils.NettyMessageDecoder;
import com.xieqingduan.priProto.utils.NettyMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NettyServer {
    public void bind(String host,int port) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss,work).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
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
                         p.addLast(new LoginAuthRespHandler());
                         p.addLast(new HeartBeatRespHandler());
                    }
                });
        ChannelFuture future = b.bind(host,port).sync();
        System.out.println("Netty Server start success!" );
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind("127.0.0.1",9999);
    }
}
