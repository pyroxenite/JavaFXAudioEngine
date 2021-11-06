package audio;

public interface SignalProvider {
    public float[] requestFrame(int frameLength);
}