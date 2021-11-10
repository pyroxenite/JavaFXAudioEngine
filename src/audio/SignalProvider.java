package audio;

public interface SignalProvider {
    public float[] requestFrames(int n);
}