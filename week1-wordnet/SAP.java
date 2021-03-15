import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private final FastBFS vBfs;
    private final FastBFS wBfs;
    private final ArrayDigraph graph;

    private int shortestPath;
    private int ancestor;

    private class CacheValue<T> {
        public T w;
        public T v;
        public int ancestor = -1;
        public int shortestPath = Integer.MAX_VALUE;

        public boolean inCache(T v, T w) {
            return this.v != null && ((v.equals(this.v) && w.equals(this.w))
                    || (w.equals(this.v) && v.equals(this.w)));
        }
    }

    private final CacheValue<Integer> valueCache = new CacheValue<>();
    private final CacheValue<Iterable<Integer>> iterablesCache = new CacheValue<>();

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph g) {
        graph = new ArrayDigraph(g);
        vBfs = new FastBFS(graph);
        wBfs = new FastBFS(graph);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        calculateShortestPath(v, w);
        return shortestPath;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        calculateShortestPath(v, w);
        return ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        calculateShortestPath(v, w);
        return shortestPath;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        calculateShortestPath(v, w);
        return ancestor;
    }

    private void calculateShortestPath(int v, int w) {
        checkSource(v);
        checkSource(w);
        if (!inCache(v, w)) {
            vBfs.startBfsInLockstep(v);
            wBfs.startBfsInLockstep(w);
            calculateShortestPath();
            updateCache(v, w);
        }
    }

    private void calculateShortestPath(Iterable<Integer> v, Iterable<Integer> w) {
        checkSource(v);
        checkSource(w);
        if (!inCache(v, w)) {
            vBfs.startBfsInLockstep(v);
            wBfs.startBfsInLockstep(w);
            calculateShortestPath();
            updateCache(v, w);
        }
    }

    private void calculateShortestPath() {
        shortestPath = Integer.MAX_VALUE;
        ancestor = -1;

        int lastV = -1;
        int lastW = -1;

        do {
            lastV = makeStep(vBfs, wBfs);
            lastW = makeStep(wBfs, vBfs);

            lastV = tryTerminate(vBfs, lastV);
            lastW = tryTerminate(wBfs, lastW);
        } while (lastV != -1 || lastW != -1);

        if (ancestor == -1) {
            shortestPath = -1;
        }
    }

    private int makeStep(FastBFS firstBfs, FastBFS secondBfs) {
        int lastVertex = firstBfs.makeStep();
        if (lastVertex != -1 && secondBfs.hasPathTo(lastVertex)) {
            int shortestCandidate = firstBfs.distanceTo(lastVertex)
                    + secondBfs.distanceTo(lastVertex);
            if (shortestCandidate < shortestPath) {
                ancestor = lastVertex;
                shortestPath = shortestCandidate;
            }
        }
        return lastVertex;
    }

    private int tryTerminate(FastBFS v, int lastVertex) {
        if (v.getCurrentDistance() > shortestPath) {
            v.terminate();
            return -1;
        }
        return lastVertex;
    }

    private void checkSource(Iterable<Integer> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("Sources are null");
        }

        for (Integer s : sources) {
            if (s == null) {
                throw new IllegalArgumentException("Source cannot be null");
            }
            checkSource(s);
        }
    }

    private void checkSource(int s) {
        if (s < 0 || s >= graph.V()) {
            throw new IllegalArgumentException("Source vertex " + s + " is out of range");
        }
    }

    private boolean inCache(Iterable<Integer> v, Iterable<Integer> w) {
        if (iterablesCache.inCache(v, w)) {
            ancestor = iterablesCache.ancestor;
            shortestPath = iterablesCache.shortestPath;
            return true;
        }
        return false;
    }

    private void updateCache(Iterable<Integer> v, Iterable<Integer> w) {
        iterablesCache.v = v;
        iterablesCache.w = w;
        iterablesCache.ancestor = ancestor;
        iterablesCache.shortestPath = shortestPath;
    }

    private boolean inCache(int v, int w) {
        if (valueCache.inCache(v, w)) {
            ancestor = valueCache.ancestor;
            shortestPath = valueCache.shortestPath;
            return true;
        }
        return false;
    }

    private void updateCache(int v, int w) {
        valueCache.v = v;
        valueCache.w = w;
        valueCache.ancestor = ancestor;
        valueCache.shortestPath = shortestPath;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
