package netcode.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            // We can't determine the size of the data payload
            return;
        }

        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();

        if (byteBuf.readableBytes() < dataLength) {
            // The entire payload has not arrived. Reset the reader.
            byteBuf.resetReaderIndex();
            return;
        }

        // We can deserialize the packet
        byte[] bytes = new byte[dataLength];
        byteBuf.readBytes(bytes);
        list.add(Serializer.fromBytes(bytes));
    }
}
