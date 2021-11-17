package main.components;

import main.interfaces.FrameGenerator;
import main.Module;
import main.modules.KnobModule;

/**
 * An input port is drawn on the left of a module and is an audio target. When connected to a source via a cable,
 * requested frames will come directly from the source. Otherwise, the returned frame will be zero.
 */
public class InputPort extends Port implements FrameGenerator {
    private Cable cable = null;

    public InputPort(String name, Module parent) {
        super(name, parent);
    }

    @Override
    public void connectTo(Port port) {
        if (port.getClass() == OutputPort.class) {
            cable = new Cable((OutputPort) port, this);
            if (port.getParent().getClass() == KnobModule.class) {
                port.getParent().setName(this.getName());
            }
        }
    }

    public void disconnect() {
        cable = null;
    }

    public float[] requestFrame(int frameLength) {
        if (cable != null && cable.isConnectedToSource()) {
            return cable.getSource().requestFrame(frameLength);
        } else {
            return ZERO_FRAME_GENERATOR.requestFrame(frameLength);
        }
    }

    public Cable getCable() {
        return cable;
    }
}
