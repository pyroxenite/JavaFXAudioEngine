package utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

final public class FormatConverter {
    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    public static byte[] toByteArray(float[] floats, int bytesPerFloat) throws IOException {
        byteArrayOutputStream.reset();
        for (int i=0; i<floats.length; i++) {
            short s = (short) (floats[i] * (1 << (8*bytesPerFloat - 1)));
            dataOutputStream.writeShort(s);
        }
        return byteArrayOutputStream.toByteArray();
    }
}

/*final public class FormatConverter {
    public static byte[] toByteArray(float[] floats, int bytesPerFloat) {
        byte[] bytes = new byte[floats.length * bytesPerFloat];

        double factor = Math.pow(2, 8*bytesPerFloat)/2;
        int maxInt = (1 << (8*bytesPerFloat-1)) - 1;
        int minInt = - (1 << (8*bytesPerFloat-1));

        for (int i=0; i<floats.length; i++) {
            int intBits = (int) (floats[i] * factor);
            intBits = Math.max(minInt, Math.min(maxInt, intBits));

            for (int j=0; j<bytesPerFloat; j++) {
                bytes[i * bytesPerFloat + j] = (byte) (intBits & 0xff);
                intBits = intBits >>> 8;
            }
        }
        return bytes;
    }

    public static void main(String[] args) {
        float[] floats = {0.5f};
        byte[] bytes = FormatConverter.toByteArray(floats, 2);
        PrettyPrinter.printBytesAsBinary(bytes);
    }
}*/

/*final public class FormatConverter {
    public static byte[] toByteArray(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i=0; i<floats.length; i++) {
            short x = (short) (floats[i] * (1 << (16-1)));
            bytes[i * 2] = (byte) (x & 0xff);
            bytes[i * 2 + 1] = (byte) ((x >> 8) & 0xff);
        }
        return bytes;
    }

    public static void main(String[] args) {
        float[] floats = {0.5f};
        byte[] bytes = FormatConverter.toByteArray(floats);
        PrettyPrinter.printBytes(bytes);
    }
}*/


