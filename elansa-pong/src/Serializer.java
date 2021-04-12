import java.io.*;

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
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(inputStream);
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
}
