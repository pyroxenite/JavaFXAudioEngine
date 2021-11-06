package audio;

import utility.PrettyPrinter;

public class AudioBuffer {
    private byte[] byteArray;
    private int writeHead = 0;
    private int readHead = 0;
    private int availableBytesCount = 0;

    public AudioBuffer(int length) {
        byteArray = new byte[length];
    }

    public void write(byte[] bytes) throws IndexOutOfBoundsException {
        if (bytes.length > byteArray.length - availableBytesCount)
            throw new IndexOutOfBoundsException("Not enough space to write bytes.");
        for (int i = 0; i < bytes.length; i++) {
            byteArray[writeHead] = bytes[i];
            writeHead = (writeHead + 1) % byteArray.length;
        }
        availableBytesCount += bytes.length;
    }

    public byte[] read(int length) throws IndexOutOfBoundsException {
        if (length > availableBytesCount)
            throw new IndexOutOfBoundsException("Cannot read more bytes than are available.");
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = byteArray[readHead];
            readHead = (readHead + 1) % byteArray.length;
        }
        availableBytesCount -= length;
        return bytes;
    }

    public static void main(String[] args) {
        AudioBuffer ab = new AudioBuffer(8);
        byte[] test = {1, 2, 3, 4, 5};
        ab.write(test);
        PrettyPrinter.printBytes(ab.byteArray);

        test = ab.read(5);
        PrettyPrinter.printBytes(test);

        ab.write(new byte[]{8, 8, 8, 8});
        PrettyPrinter.printBytes(ab.byteArray);
        PrettyPrinter.printBytes(ab.read(3));
        PrettyPrinter.printBytes(ab.read(1));
    }
}