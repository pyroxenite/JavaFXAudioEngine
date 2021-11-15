package main.components;

import main.Module;

/**
 * An output port is drawn on the right of a module and is an audio source. A target input port connected to an output
 * port via a cable may request frames.
 */
public class OutputPort extends Port {
    public OutputPort(String name, Module parent) {
        super(name, parent);
    }

    @Override
    public void connectTo(Port port) {
        if (port.getClass() == InputPort.class)
            port.connectTo(this);
    }
}
