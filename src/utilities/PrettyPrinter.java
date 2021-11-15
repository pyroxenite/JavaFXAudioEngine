package utilities;

final public class PrettyPrinter {
    public static void printBytes(byte[] bytes) {
        System.out.print("[ ");
        for (int i=0; i<bytes.length; i++) {
            System.out.print(bytes[i]);
            System.out.print((i != bytes.length - 1)?", ":" ]\n");
        }
    }

    public static void printBytesAsBinary(byte[] bytes) {
        System.out.print("[ ");
        for (int i=0; i<bytes.length; i++) {
            System.out.print(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
            System.out.print((i != bytes.length - 1)?", ":" ]\n");
        }
    }

    public static void printFloats(float[] floats) {
        System.out.print("[ ");
        for (int i=0; i<floats.length; i++) {
            System.out.print(floats[i]);
            System.out.print((i != floats.length - 1)?", ":" ]\n");
        }
    }
}
