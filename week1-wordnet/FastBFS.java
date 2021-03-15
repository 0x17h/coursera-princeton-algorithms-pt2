import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;

public class FastBFS {
    private class Vertex {
        public static final int MAX_DISTANCE = Integer.MAX_VALUE;

        public int distanceTo = MAX_DISTANCE;
        public int edgeTo;
        public boolean isMarked;

        public void reinitialize() {
            distanceTo = MAX_DISTANCE;
            edgeTo = 0;
            isMarked = false;
        }
    }

    private int currentDistance = 0;
    private final Vertex[] vertexes;
    private Queue<Integer> nextStepQueue = new Queue<Integer>();
    private Bag<Vertex> dirtyQueue = new Bag<Vertex>();

    private final Digraph graph;

    FastBFS(Digraph g) {
        if (g == null) {
            throw new IllegalArgumentException("Graph is null");
        }

        graph = g;
        vertexes = new Vertex[g.V()];
        for (int i = 0; i < vertexes.length; ++i) {
            vertexes[i] = new Vertex();
        }
    }

    public void bfs(Iterable<Integer> sources) {
        startBfsInLockstep(sources);
        while (makeStep() != -1) {

        }
    }

    public void bfs(int source) {
        startBfsInLockstep(source);
        while (makeStep() != -1) {

        }
    }

    public void startBfsInLockstep(Iterable<Integer> sources) {
        checkSources(sources);
        reinitialize();

        for (int s : sources) {
            addSource(s);
        }
    }

    public void startBfsInLockstep(int source) {
        checkSource(source);
        reinitialize();
        addSource(source);
    }

    public int makeStep() {
        if (nextStepQueue.isEmpty()) {
            return -1;
        }

        int v = nextStepQueue.dequeue();
        currentDistance = vertexes[v].distanceTo;

        for (int w : graph.adj(v)) {
            Vertex vertex = vertexes[w];
            if (!vertex.isMarked) {
                vertex.isMarked = true;
                vertex.distanceTo = currentDistance + 1;
                vertex.edgeTo = v;

                dirtyQueue.add(vertex);
                nextStepQueue.enqueue(w);
            }
        }

        return v;
    }

    public int distanceTo(int w) {
        checkSource(w);
        return vertexes[w].distanceTo;
    }

    public boolean hasPathTo(int w) {
        checkSource(w);
        return vertexes[w].isMarked;
    }

    public int getCurrentDistance() {
        return currentDistance;
    }

    private void addSource(int s) {
        Vertex v = vertexes[s];
        v.isMarked = true;
        v.distanceTo = 0;
        nextStepQueue.enqueue(s);
        dirtyQueue.add(v);
    }

    private void checkSources(Iterable<Integer> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("Sources are null");
        }

        int counter = 0;
        for (int s : sources) {
            ++counter;
            checkSource(s);
        }

        if (counter == 0) {
            throw new IllegalArgumentException("Count of sources is 0");
        }
    }

    private void checkSource(int s) {
        if (s < 0 || s >= graph.V()) {
            throw new IllegalArgumentException("Source vertex " + s + " is out of range");
        }
    }

    private void reinitialize() {
        for (Vertex v : dirtyQueue) {
            v.reinitialize();
        }

        if (!dirtyQueue.isEmpty()) {
            dirtyQueue = new Bag<Vertex>();
        }

        if (!nextStepQueue.isEmpty()) {
            nextStepQueue = new Queue<Integer>();
        }

        currentDistance = 0;
    }

    public Iterable<Integer> pathTo(int v) {
        checkSource(v);

        if (!hasPathTo(v)) {
            return null;
        }

        Stack<Integer> path = new Stack<Integer>();
        int x = v;
        for (; vertexes[x].distanceTo != 0; x = vertexes[x].edgeTo) {
            path.push(x);
        }
        path.push(x);

        return path;
    }

    public static void main(String[] args) {

    }
}
