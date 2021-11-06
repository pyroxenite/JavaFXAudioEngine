// TEST


package audio;

public class Test {
    public static void main(String[] args) {
        short x = 1;
        byte[] ret = new byte[2];

        ret[0] = (byte) x;
        ret[1] = (byte) (x >> 8);

        String s1 = String.format("%8s", Integer.toBinaryString(ret[0] & 0xFF)).replace(' ', '0');
        System.out.println(s1);

        s1 = String.format("%8s", Integer.toBinaryString(ret[1] & 0xFF)).replace(' ', '0');
        System.out.println(s1);
    }
}
