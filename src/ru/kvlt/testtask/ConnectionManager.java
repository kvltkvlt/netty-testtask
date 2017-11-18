package ru.kvlt.testtask;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ConnectionManager {

    private static ConnectionManager instance;

    private boolean isStarted;
    private int port;
    private ChannelFuture channelFuture;

    private ConnectionManager() {
        port = 9000;
    }

    public void start() {
        if (isStarted) return;

        boolean hasEpoll = Epoll.isAvailable();

        EventLoopGroup boss;
        EventLoopGroup worker;
        Class<? extends ServerSocketChannel> channelClass;

        if (hasEpoll) {
            boss = new EpollEventLoopGroup();
            worker = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
        }

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(channelClass)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ServerInializer());

            channelFuture = bootstrap.bind(port).sync();
            channelFuture.addListener((future) -> Log.info(
                    future.isSuccess()
                            ? "Сервер запущен, порт " + port
                            : "Не удалось запустить сервер: " + future.cause()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized ConnectionManager get() {
        return instance == null ? instance = new ConnectionManager() : instance;
    }

}