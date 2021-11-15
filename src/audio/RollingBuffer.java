package audio;

import utilities.PrettyPrinter;

public class FloatQueue {
    private float[] queueContainer;
    private int tail = 0;

    public FloatQueue(int length) {
        queueContainer = new float[length];
    }

    public void add(float value) {
        queueContainer[tail] = value;
        tail = (tail - 1 + queueContainer.length) % queueContainer.length;
    }

    public float[] getQueue() {
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
        FloatQueue q = new FloatQueue(5);
        q.add(1.5f);
        PrettyPrinter.printFloats(q.getQueue());

        q.add(0.5f);
        PrettyPrinter.printFloats(q.getQueue());

        q.add(3f);
        PrettyPrinter.printFloats(q.getQueue());

        q.add(6f);
        PrettyPrinter.printFloats(q.getQueue());

        q.add(2f);
        PrettyPrinter.printFloats(q.getQueue());

        q.add(2.3f);
        PrettyPrinter.printFloats(q.getQueue());
    }
}
