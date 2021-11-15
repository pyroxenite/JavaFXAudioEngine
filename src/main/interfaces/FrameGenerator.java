package main;

/**
 * Any object that is able to supply audio frames via the `requestFrame` method.
 */
public interface FrameProvider {
    float[] requestFrame(int frameLength);
}
