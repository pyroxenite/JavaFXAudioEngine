package main.components;

import main.Module;

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
