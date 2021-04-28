package netcode.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
        // We encode a packet as a byte stream by putting the data length and then the data
        byte[] bytes = Serializer.getBytes(packet);
        int dataLength = bytes.length;
        byteBuf.writeInt(dataLength);
        byteBuf.writeBytes(bytes);
    }
}
