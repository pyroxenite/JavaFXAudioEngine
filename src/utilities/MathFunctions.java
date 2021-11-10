package utilities;

final public class MathFunctions {
    public static double lerp(double a, double b, double t) {
        return (1-t)*a + t*b;
    }

    public static double rootMeanSquare(float[] frame) {
        float sum = 0;
        for (int i = 0; i<frame.length; i++)
            sum += frame[i]*frame[i];
        return Math.sqrt(sum / frame.length);
    }

    public static double amplitudeInDecibels(float[] frame) {
        return 20 * Math.log10(rootMeanSquare(frame));
    }
}
