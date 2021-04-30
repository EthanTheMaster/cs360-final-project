package netcode.packets;

import game.AbstractLocalGame;
import game.GameSettings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class Serializer {
    public static <T extends Serializable> byte[] getBytes(T object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream output;
        try {
            output = new ObjectOutputStream(outputStream);
            output.writeObject(object);
            output.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static Object fromBytes(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ValidatingObjectInputStream input = null;
        try {
            input = new ValidatingObjectInputStream(inputStream);
            input.accept(GameSettings.SERIALIZABLE_WHITELIST);

            return input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void sendPacketUdp(Channel udpChannel, String dstAddr, int dstPort, Packet packet) {
        byte[] bytes = Serializer.getBytes(packet);
        ByteBuf buffer = udpChannel.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);

        DatagramPacket datagram = new DatagramPacket(buffer, new InetSocketAddress(dstAddr, dstPort));
        udpChannel.writeAndFlush(datagram)
        .addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                f.cause().printStackTrace();
            }
        });
    }

    public static Packet decodeUdpDatagram(DatagramPacket datagram) {
        ByteBuf buffer = datagram.content();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        Object obj = Serializer.fromBytes(bytes);
        return (Packet) Serializer.fromBytes(bytes);
    }

    public static AbstractLocalGame readGameMapFromFile(File gameMap) throws IOException, ClassNotFoundException {
        // Attempt to serialize the file
        ObjectInputStream objectInputStream = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(gameMap);
            objectInputStream = new ObjectInputStream(fileInputStream);

            return (AbstractLocalGame) objectInputStream.readObject();
        } finally {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        }
    }
}
