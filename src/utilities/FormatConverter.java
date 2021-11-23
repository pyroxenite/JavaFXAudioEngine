package utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*final public class FormatConverter {
    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    public static byte[] toByteArray(float[] floats, int bytesPerFloat) throws IOException {
        int amplitude = (int) (1 << (8*bytesPerFloat - 1));
        System.out.println(amplitude);
        byteArrayOutputStream.reset();
        for (int i=0; i<floats.length; i++) {
            short s = (short) (floats[i] * amplitude);
            System.out.println(s);
            dataOutputStream.writeShort(s);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        float[] floats = new float[30];
        for (int i=0; i<floats.length; i++)
            floats[i] = -0.0000305f * i;
        PrettyPrinter.printBytes(toByteArray(floats, 2));
    }
}*/

final public class FormatConverter {
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
}

/*final public class FormatConverter {
    public static byte[] toByteArray(float[] floats, int o) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i=0; i<floats.length; i++) {
            short val = (short) (Math.abs(floats[i]) * 32767);
            if (Math.copySign(1, floats[i]) == 1f) {
                bytes[i * 2] = (byte) (val & 0x00ff);
                bytes[i * 2 + 1] = (byte) ((val & 0xff00) >>> 8);
            } else {
                bytes[i * 2] = (byte) -(val & 0x00ff);
                bytes[i * 2 + 1] = (byte) -((val & 0xff00) >>> 8);
            }
        }
        return bytes;
    }

    public static void main(String[] args) throws IOException {
        float[] floats = new float[100];
        for (int i=0; i<floats.length; i++)
            floats[i] = 0.0000305f * (i - floats.length/2) ;
        PrettyPrinter.printBytes(toByteArray(floats, 2));
    }
}*/


