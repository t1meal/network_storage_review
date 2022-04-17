package com.lezenford.netty.advanced.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lezenford.netty.advanced.common.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        System.out.println("incoming message: " + msg);
        Message message = OBJECT_MAPPER.readValue(msg, Message.class);
        out.add(message);
    }
}
