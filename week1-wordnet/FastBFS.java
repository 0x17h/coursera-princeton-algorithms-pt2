import edu.princeton.cs.algs4.Stack;

public class FastBFS {
    private static final int IS_MARKED_OFFSET = 0;
    private static final int EDGE_TO_OFFSET = 1;
    private static final int DISTANCE_TO_OFFSET = 2;
    private static final int PROPS_COUNT = 3;

    private final int[] vertexes;
    private int currentMarkedValue = Integer.MAX_VALUE;

    private int currentDistance = 0;

    private int queueStartIndex = 0;
    private int queueEndIndex = 0;
    private final int[] nextStepQueue;

    private final ArrayDigraph graph;

    FastBFS(ArrayDigraph g) {
        if (g == null) {
            throw new IllegalArgumentException("Graph is null");
        }

        graph = g;
        vertexes = new int[g.V() * PROPS_COUNT];
        nextStepQueue = new int[g.V()];
        reinitialize();
    }

    public void startBfsInLockstep(Iterable<Integer> sources) {
        reinitialize();
        for (int s : sources) {
            addSource(s);
        }
    }

    public void startBfsInLockstep(int source) {
        reinitialize();
        addSource(source);
    }

    public int makeStep() {
        if (isTerminated()) {
            return -1;
        }

        int v = dequeue();
        currentDistance = vertexes[v * PROPS_COUNT + DISTANCE_TO_OFFSET];

        for (int w : graph.adj(v)) {
            int vertexBase = w * PROPS_COUNT;
            if (vertexes[vertexBase + IS_MARKED_OFFSET] != currentMarkedValue) {
                vertexes[vertexBase + IS_MARKED_OFFSET] = currentMarkedValue;
                vertexes[vertexBase + DISTANCE_TO_OFFSET] = currentDistance + 1;
                vertexes[vertexBase + EDGE_TO_OFFSET] = v;

                enqueue(w);
            }
        }

        return v;
    }

    public void terminate() {
        queueEndIndex = 0;
        queueStartIndex = 0;
    }

    public boolean isTerminated() {
        // return nextStepQueue.isEmpty();
        return queueEndIndex == queueStartIndex;
    }

    public int distanceTo(int w) {
        return vertexes[w * PROPS_COUNT + DISTANCE_TO_OFFSET];
    }

    public boolean hasPathTo(int w) {
        return vertexes[w * PROPS_COUNT + IS_MARKED_OFFSET] == currentMarkedValue;
    }

    public int getCurrentDistance() {
        return currentDistance;
    }

    private void addSource(int s) {
        int base = s * PROPS_COUNT;
        vertexes[base + IS_MARKED_OFFSET] = currentMarkedValue;
        vertexes[base + DISTANCE_TO_OFFSET] = 0;
        // nextStepQueue.enqueue(s);
        enqueue(s);
    }

    private void reinitialize() {
        terminate();
        currentDistance = 0;

        if (currentMarkedValue == Integer.MAX_VALUE) {
            for (int i = 0; i < vertexes.length; i += PROPS_COUNT) {
                vertexes[i + IS_MARKED_OFFSET] = Integer.MIN_VALUE;
                vertexes[i + EDGE_TO_OFFSET] = 0;
                vertexes[i + DISTANCE_TO_OFFSET] = Integer.MAX_VALUE;
            }
            currentMarkedValue = Integer.MIN_VALUE;
        }

        ++currentMarkedValue;
    }

    private void enqueue(int v) {
        nextStepQueue[queueEndIndex++] = v;
    }

    private int dequeue() {
        return nextStepQueue[queueStartIndex++];
    }

    public Iterable<Integer> pathTo(int v) {
        if (!hasPathTo(v)) {
            return null;
        }

        Stack<Integer> path = new Stack<Integer>();
        int x = v;
        for (; vertexes[x + DISTANCE_TO_OFFSET] != 0; x = vertexes[x + EDGE_TO_OFFSET]) {
            path.push(x);
        }
        path.push(x);

        return path;
    }

    public static void main(String[] args) {

    }
}
