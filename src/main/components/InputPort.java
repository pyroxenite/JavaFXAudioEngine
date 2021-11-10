package main.components;

import main.Module;
import main.modules.KnobModule;

public class InputPort extends Port {
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

    public float[] requestFrames(int frameLength) {
        if (cable != null && cable.isConnectedToSource()) {
            return cable.getSource().requestFrames(frameLength);
        } else {
            return zeroSignalProvider.requestFrames(frameLength);
        }
    }

    public Cable getCable() {
        return cable;
    }
}
