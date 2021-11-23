package main;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import main.interfaces.Drawable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilities.ColorTheme;
import utilities.Point;
import main.components.Cable;
import main.components.InputPort;
import main.components.OutputPort;
import main.components.Port;

import javafx.scene.input.MouseEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Modules are the audio processing/IO units. They provide a GUI for users to interact with the audio pipeline in
 * realtime. This base class supports an arbitrary amount of inputs and outputs but the latter deliver a signal equal
 * to zero by default. Further action through `OutputPort.setFrameGenerator(int frameLength)` is required to make the
 * module do anything useful.
 */
public class Module implements Drawable {
    // All the following attributes are protected because they are used in subclasses.
    protected String name;
    protected UUID uuid = UUID.randomUUID();

    protected Point position = new Point(0, 0);
    protected int width = 150;
    protected int height = 200;
    protected ArrayList<InputPort> inputs = new ArrayList<>();
    protected ArrayList<OutputPort> outputs = new ArrayList<>();

    protected boolean dragStarted = false;
    protected boolean isSelected = false;
    protected Port portUnderMouse = null;

    protected Cable temporaryCableReference = null;

    public Module(String name) {
        this.name = name;
        updateGeometry();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPosition() {
        return position;
    }

    /**
     * Sets the position of the module. This is the module's top-left corner.
     * @return Returns the module to allow for method chaining.
     */
    public Module setPosition(double x, double y) {
        this.position.setX(x);
        this.position.setY(y);
        return this;
    }

    public Module setPosition(Point position) {
        this.position = position;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<InputPort> getInputs() {
        return inputs;
    }

    public ArrayList<OutputPort> getOutputs() {
        return outputs;
    }

    public InputPort getInput(int i) {
        return inputs.get(i);
    }

    public OutputPort getOutput(int i) {
        return outputs.get(i);
    }

    /**
     * Creates a new input port for the module and returns it to enable method chaining.
     * @param name The name of the input (used as a label).
     * @return The created port.
     */
    public InputPort addInput(String name) {
        InputPort port = new InputPort(name, this);
        inputs.add(port);
        updateGeometry();
        return port;
    }

    /**
     * Creates a new output port for the module and returns it to enable method chaining.
     * @param name The name of the output (used as a label).
     * @return The created port.
     */
    public OutputPort addOutput(String name) {
        OutputPort port = new OutputPort(name, this);
        outputs.add(port);
        updateGeometry();
        return port;
    }

    /**
     * Constructs the module's bounding box.
     * @return The bounding box.
     */
    public Rectangle2D getBoundingBox() {
        return new Rectangle2D(
                position.getX(),
                position.getY(),
                width,
                height
        );
    }

    public void setUUID(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void newRandomUUID() {
        uuid = UUID.randomUUID();
    }

    /**
     * Calculates the layout of the module. This includes height and width based on the size of the labels as well as
     * positioning the ports automatically.
     */
    protected void updateGeometry() {
        int textLineHeight = 22;

        int maxInputLabelWidth = 15;
        for (Port input: inputs) {
            maxInputLabelWidth = Math.max(
                    maxInputLabelWidth,
                    (int) new Text(input.getName()).getBoundsInLocal().getWidth() + 22
            );
        }
        int maxOutputLabelWidth = 15;
        for (Port output: outputs) {
            maxOutputLabelWidth = Math.max(
                    maxOutputLabelWidth,
                    (int) new Text(output.getName()).getBoundsInLocal().getWidth() + 22
            );
        }
        this.width = maxInputLabelWidth + maxOutputLabelWidth;
        if (maxInputLabelWidth != 15 && maxOutputLabelWidth != 15)
            this.width += 15;

        int titleWidth = (int) new Text(name).getBoundsInLocal().getWidth() + 40;
        width = Math.max(titleWidth, width);

        height = (int) ((Math.max(outputs.size(), inputs.size()) + 1) * textLineHeight + 14);
        height = Math.max(40, height);

        int x = 11;
        int y = (int) ((Math.max(0, outputs.size() - inputs.size())) * textLineHeight / 2 + 20);
        for (Port input: inputs) {
            y += textLineHeight;
            input.setPosition(x, y);
        }

        x = this.width - 11;
        y = (int) ((Math.max(0, inputs.size() - outputs.size())) * textLineHeight / 2 + 20);
        for (Port output: outputs) {
            y += textLineHeight;
            output.setPosition(x, y);
        }
    }

    /**
     * Draws the module at its position.
     * @param gc The current graphics context.
     */
    public void draw(GraphicsContext gc) {
        // Translate to the module's position.
        translate(gc, this.position.getX(), this.position.getY());

        drawPaneAndTitleBar(gc);
        drawPortsAndLabels(gc);

        // Invert translation.
        translate(gc, -this.position.getX(), -this.position.getY());
        drawCables(gc);
    }

    /**
     * Translate drawing actions by (x, y).
     * @param gc The current graphics context.
     */
    protected void translate(GraphicsContext gc, double x, double y) {
        gc.transform(new Affine(1, 0, x,0, 1, y));
    }

    /**
     * Draws the module's background, borders and title.
     * @param gc The current graphics context.
     */
    protected void drawPaneAndTitleBar(GraphicsContext gc) {
        gc.setFill(ColorTheme.MODULE_BACKGROUND);
        if (isSelected) {
            gc.setLineWidth(2);
            gc.setStroke(ColorTheme.MODULE_BORDER_SELECTED);
        } else {
            gc.setLineWidth(1);
            gc.setStroke(ColorTheme.MODULE_BORDER);
        }
        double borderRadius = 30;
        gc.fillRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.strokeRoundRect(0, 0, width, height, borderRadius, borderRadius);
        gc.setFill(ColorTheme.TEXT_NORMAL);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(name, width/2, 18, width-20);

        gc.setStroke(ColorTheme.MODULE_BORDER);
        gc.setLineWidth(1);
        if (isSelected) {
            gc.strokeLine(1, 26, width-1, 26);
        } else {
            gc.strokeLine(0, 26, width, 26);
        }
    }

    /**
     * Draws the input and output ports with their corresponding labels.
     * @param gc The current graphics context.
     */
    protected void drawPortsAndLabels(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.LEFT);
        for (InputPort inputPort: inputs) {
            inputPort.draw(gc);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(inputPort.getName(), inputPort.getPosition().getX()+10, inputPort.getPosition().getY()+4);
        }

        gc.setTextAlign(TextAlignment.RIGHT);
        for (OutputPort outputPort: outputs) {
            outputPort.draw(gc);
            gc.setFill(ColorTheme.TEXT_NORMAL);
            gc.fillText(outputPort.getName(), outputPort.getPosition().getX()-10, outputPort.getPosition().getY()+4);
        }
    }

    /**
     * Draws the input and output ports without their corresponding labels.
     * @param gc The current graphics context.
     */
    protected void drawPortsOnly(GraphicsContext gc) {
        inputs.forEach(inputPort -> inputPort.draw(gc));
        outputs.forEach(outputPort -> outputPort.draw(gc));
    }

    /**
     * Draws cables connected to the inputs as well as the temporary cable used during drag operations.
     * @param gc The current graphics context.
     */
    protected void drawCables(GraphicsContext gc) {
        if (temporaryCableReference != null)
            temporaryCableReference.draw(gc);

        inputs.forEach(inputPort -> {
            Cable cable = inputPort.getCable();
            if (cable != null) cable.draw(gc);
        });
    }

    /**
     * Iterates through ports until a port at the specified relative position is found. The position is relitive to the
     * module's position (top-left corner).
     * @param relativeMousePosition The relative position.
     * @return A port if one is found. Null otherwise.
     */
    public Port findPortUnderMouse(Point relativeMousePosition) {
        for (Port inputPort: inputs) {
            if (inputPort.getPosition().distanceTo(relativeMousePosition) < 7) {
                return inputPort;
            }
        }
        for (Port outputPort: outputs) {
            if (outputPort.getPosition().distanceTo(relativeMousePosition) < 7) {
                return outputPort;
            }
        }
        return null;
    }

    /**
     * Handles mouse clicks that occur in the module's bounding box.
     * @param event The mouse event triggered by the user.
     */
    public void handleMouseClicked(MouseEvent event) {
        Point mousePosition = new Point((int) event.getX(), (int) event.getY());
        Point relativePosition = mousePosition.copy().subtract(position);
        if (relativePosition.getY() < 26) {
            dragStarted = true;
        } else {
            portUnderMouse = findPortUnderMouse(relativePosition);
            if (portUnderMouse != null) {
                if (portUnderMouse.getClass() == OutputPort.class)
                    temporaryCableReference = new Cable((OutputPort) portUnderMouse, mousePosition);
                else {
                    ((InputPort) portUnderMouse).disconnect();
                    temporaryCableReference = new Cable((InputPort) portUnderMouse, mousePosition);
                }
            }
        }
    }

    /**
     * Handle a drag event when the drag was initiated in the module's bounding box.
     * @param mousePosition The absolute mouse position.
     * @param mouseDelta The difference between the current and previous mouse position.
     */
    public void handleDrag(Point mousePosition, Point mouseDelta) {
        if (dragStarted) {
            setPosition(
                    position.getX() + mouseDelta.getX(),
                    position.getY() + mouseDelta.getY()
            );
        } else if (temporaryCableReference != null && portUnderMouse != null) {
            temporaryCableReference.setLooseEndPosition(mousePosition);
        }
    }

    /**
     * Handles mouse releases after a mouse click that has occurred in the module's bounding box.
     * @param mousePosition The absolute position of the mouse.
     * @param moduleUnderMouse The module that was under the mouse at time of release. It can be different that the
     *                         current module (for example when dragging a cable to another module's port).
     */
    public void handleMouseReleased(Point mousePosition, Module moduleUnderMouse) {
        dragStarted = false;

        temporaryCableReference = null;

        if (moduleUnderMouse != null && moduleUnderMouse != this) {
            Point relativePosition = mousePosition.copy().subtract(moduleUnderMouse.getPosition());
            Port externalPortUnderMouse = moduleUnderMouse.findPortUnderMouse(relativePosition);

            if (externalPortUnderMouse != null && portUnderMouse.getClass() != externalPortUnderMouse.getClass()) {
                portUnderMouse.connectTo(externalPortUnderMouse);
            }
        }
    }

    /**
     * Set the module as selected (changes the way it is drawn).
     */
    public void select() {
        isSelected = true;
    }

    /**
     * Deselects the module.
     */
    public void deselect() {
        isSelected = false;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("name", name);

        obj.put("class", "Module");
        obj.put("uuid", uuid.toString());
        obj.put("x-position", position.getX());
        obj.put("y-position", position.getY());
        JSONArray inputPortsJSON = new JSONArray();
        inputs.forEach(port -> inputPortsJSON.add(port.getName()));
        obj.put("inputs", inputPortsJSON);
        JSONArray outputPortsJSON = new JSONArray();
        outputs.forEach(port -> outputPortsJSON.add(port.getName()));
        obj.put("outputs", outputPortsJSON);

        return obj;
    }

    public static Module fromJSON(String str) throws ParseException {
        JSONParser parser = new JSONParser();
        return fromJSON(parser.parse(str));
    }

    public static Module fromJSON(Object obj) {
        return fromJSON((JSONObject) obj);
    }

    public static Module fromJSON(JSONObject obj) {
        Module module = new Module((String) obj.getOrDefault("name", ""));

        JSONArray inputPortNames = (JSONArray) obj.getOrDefault("inputs", new JSONArray());
        for (Object o: inputPortNames) {
            String s = (String) o;
            module.addInput(s);
        }
        JSONArray outputPortNames = (JSONArray) obj.getOrDefault("outputs", new JSONArray());
        for (Object o: outputPortNames) {
            String s = (String) o;
            module.addOutput(s);
        }

        return module;
    }



    public static void main(String[] args) {
        Module m = new Module("This is a test");
        m.addInput("Test");
        m.addOutput("Output");
        m.setPosition(30, 40);
        System.out.println(m.toJSON());
    }

    public void prepareForDelete() {
        // Meant to be overridden
    }

    public JSONObject getInputConnectionsAsJSON() {
        JSONObject inputConnections = new JSONObject();

        for (int i=0; i < inputs.size(); i++) {
            Cable cable = inputs.get(i).getCable();
            if (cable != null) {
                Port outputPort = cable.getSource();
                int outputPortIndex = outputPort.getParent().getOutputs().indexOf(outputPort);
                JSONObject connection = new JSONObject();
                connection.put("source-module-UUID", outputPort.getParent().getUUID().toString());
                connection.put("output-port-index", outputPortIndex);
                inputConnections.put(Integer.toString(i), connection);
            }
        }

        return inputConnections;
    }
}
