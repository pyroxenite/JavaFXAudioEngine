package main.interfaces;

import javafx.scene.canvas.GraphicsContext;

/**
 * Anything that can be drawn to the screen. Typically, UI elements.
 */
public interface Drawable {
    public void draw(GraphicsContext gc);
}
