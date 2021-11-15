package main.interfaces;

/**
 * Any object that is able to supply audio frames via the `requestFrame` method.
 */
public interface FrameGenerator {
    float[] requestFrame(int frameLength);
}
