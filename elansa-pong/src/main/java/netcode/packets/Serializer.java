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
    /**
     * Helper method to convert a serializable object to an array of bytes
     * @param object the object to serialize
     * @return the serialized object as a byte array
     */
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

    /**
     * Helper method to deserialized a byte array into an object
     * @param bytes the byte array of the serialized object
     * @return the deserialized object or null if serialization fails
     */
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

    /**
     * Helper method to send a packet over UDP
     * @param udpChannel the UDP channel to send the packet over
     * @param dstAddr the destination address
     * @param dstPort the destination port
     * @param packet the packet to send
     */
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

    /**
     * Helper method to decode a received UDP datagram into a packet
     * @param datagram the received datagram
     * @return the decoded packet
     */
    public static Packet decodeUdpDatagram(DatagramPacket datagram) {
        ByteBuf buffer = datagram.content();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        Object obj = Serializer.fromBytes(bytes);
        return (Packet) Serializer.fromBytes(bytes);
    }

    /**
     * Helper method to read an exported game from a file
     * @param gameMap file pointing to the exported game
     * @return the decoded game
     * @throws IOException
     * @throws ClassNotFoundException
     */
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
