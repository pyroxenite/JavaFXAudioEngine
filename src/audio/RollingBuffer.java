package audio;

import utilities.PrettyPrinter;

public class RollingBuffer {
    private float[] queueContainer;
    private int tail = 0;

    public RollingBuffer(int length) {
        queueContainer = new float[length];
    }

    public void add(float value) {
        queueContainer[tail] = value;
        tail = (tail - 1 + queueContainer.length) % queueContainer.length;
    }

    public float[] getBuffer() {
        float[] queue = new float[queueContainer.length];
        int readHead = (tail + 1) % queueContainer.length;;
        int i = 0;
        while (readHead != tail) {
            queue[i] = queueContainer[readHead];
            i++;
            readHead = (readHead + 1) % queueContainer.length;
        }
        queue[i] = queueContainer[readHead];
        return queue;
    }

    public static void main(String[] args) {
        RollingBuffer q = new RollingBuffer(5);
        q.add(1.5f);
        PrettyPrinter.printFloats(q.getBuffer());

        q.add(0.5f);
        PrettyPrinter.printFloats(q.getBuffer());

        q.add(3f);
        PrettyPrinter.printFloats(q.getBuffer());

        q.add(6f);
        PrettyPrinter.printFloats(q.getBuffer());

        q.add(2f);
        PrettyPrinter.printFloats(q.getBuffer());

        q.add(2.3f);
        PrettyPrinter.printFloats(q.getBuffer());
    }
}
