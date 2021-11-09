package utilities;

final public class FormatConverter {
    public static byte[] toByteArray(float[] floats, int bytesPerFloat) {
        double factor = Math.pow(2, 8*bytesPerFloat)/2;
        int maxInt = (1 << (8*bytesPerFloat-1)) - 1;
        int minInt = - (1 << (8*bytesPerFloat-1));
        //System.out.println(maxInt);
        byte[] bytes = new byte[floats.length * bytesPerFloat];
        for (int i=0; i<floats.length; i++) {
            int intBits = (int) (floats[i] * factor);
            intBits = Math.max(minInt, Math.min(maxInt, intBits));
            for (int j=0; j<bytesPerFloat; j++) {
                //System.out.println(intBits);
                bytes[i * bytesPerFloat + j] = (byte) (intBits & 0xff);
                intBits = intBits >> 8;
            }
        }
        return bytes;
    }

    public static void main(String[] args) {
        float[] floats = {10f};
        byte[] bytes = FormatConverter.toByteArray(floats, 2);
        PrettyPrinter.printBytesAsBinary(bytes);
    }
}
