package utility;

public class Point {
    private double x = 0;
    private double y = 0;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Point() {}

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point copy() {
        return new Point(x, y);
    }

    public Point add(Point position) {
        x += position.getX();
        y += position.getY();
        return this;
    }

    public Point subtract(Point position) {
        x -= position.getX();
        y -= position.getY();
        return this;
    }

    public double distanceTo(Point other) {
        Point delta = copy().subtract(other);
        return Math.sqrt(Math.pow(delta.getX(), 2) + (Math.pow(delta.getY(), 2)));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
