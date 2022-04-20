package com.lezenford.netty.advanced.server;

import com.lezenford.netty.advanced.common.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;


public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private int counter = 0;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection");
        ctx.writeAndFlush(answer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {

        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("incoming text message: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DateMessage) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) msg;
            String clientLogin = message.getLogin();
            String clientPassword = message.getPassword();
            if (message.toString().contains(clientLogin) && message.toString().contains(clientPassword)) {
                System.out.println("incoming authorization successful!");
                ctx.writeAndFlush("true");
            } else {
                System.out.println("incoming authorization unsuccessful!");
                ctx.writeAndFlush("false");
            }
        }

        if (msg instanceof FileRequestMessage){
            FileRequestMessage frm = (FileRequestMessage) msg;
            final File file = new File(frm.getPath());
            try (RandomAccessFile accessFile = new RandomAccessFile(file,"r")){
                while (accessFile.getFilePointer() != accessFile.length()) {
                    byte[] fileContent;
                    final long available = accessFile.length() - accessFile.getFilePointer();
                    if (available > 64 * 1024){
                        fileContent = new byte[64 * 1024];
                    } else {
                        fileContent = new byte[(int) available];
                    }
                    FileContentMessage message = new FileContentMessage();
                    message.setStartPosition(accessFile.getFilePointer());
                    accessFile.read(fileContent);
                    message.setContent(fileContent);
                    message.setLast(accessFile.getFilePointer() == accessFile.length());
                    ctx.writeAndFlush(message);
                    System.out.println("message send:" + ++counter);
                }
            }catch (IOException e){
                throw new RuntimeException();
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("client disconnect");
    }
}
