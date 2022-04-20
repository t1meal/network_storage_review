package com.lezenford.netty.advanced.client;

import com.lezenford.netty.advanced.common.handler.JsonDecoder;
import com.lezenford.netty.advanced.common.handler.JsonEncoder;
import com.lezenford.netty.advanced.common.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


import java.io.*;


public class Client {

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    //максимальный размер сообщения равен 1024*1024 байт, в начале сообщения пдля хранения длины зарезервировано 3 байта,
                                    //которые отбросятся после получения всего сообщения и передачи его дальше по цепочке
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    //Перед отправкой добавляет в начало сообщение 3 байта с длиной сообщения
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) {
                                            FileRequestMessage frmessage = new FileRequestMessage();
                                            frmessage.setPath("F:\\Eric Wing.pdf");
                                            ctx.writeAndFlush(frmessage);

                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
//                                            System.out.println("receive mess!");
                                            if(msg instanceof FileContentMessage){
                                                FileContentMessage fcm = (FileContentMessage) msg;
                                                try (RandomAccessFile accessFile = new RandomAccessFile("F:\\3.pdf", "rw")){
                                                    accessFile.seek(fcm.getStartPosition());
                                                    accessFile.write(fcm.getContent());
                                                    if (fcm.isLast()){
                                                        ctx.close();
                                                    }
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                        }


                                    }
                            );
                        }
                    });

            System.out.println("Client started");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();

//            while (channel.isActive()) {
//                TextMessage textMessage = new TextMessage();
//                textMessage.setText(String.format("[%s] %s", LocalDateTime.now(), Thread.currentThread().getName()));
//                System.out.println("Try to send message: " + textMessage);
//                channel.writeAndFlush(textMessage);
//
//                DateMessage dateMessage = new DateMessage();
//                dateMessage.setDate(new Date());
//                channel.write(dateMessage);
//                System.out.println("Try to send message: " + dateMessage);
//                channel.flush();
//                Thread.sleep(3000);
//            }

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
