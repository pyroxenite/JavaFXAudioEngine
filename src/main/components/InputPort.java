package main.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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

    public float[] requestFrame(int frameLength) {
        if (cable != null && cable.isConnectedToSource()) {
            return cable.getSource().requestFrame(frameLength);
        } else {
            return zeroSignalProvider.requestFrame(frameLength);
        }
    }

    public Cable getCable() {
        return cable;
    }
}
