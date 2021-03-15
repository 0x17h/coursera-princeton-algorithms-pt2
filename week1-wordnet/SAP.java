import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private final FastBFS vBfs;
    private final FastBFS wBfs;

    private class Ancestor {
        public final int ancestor;
        public final int length;

        Ancestor(int ancestor, int length) {
            this.ancestor = ancestor;
            this.length = length;
        }
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph g) {
        vBfs = new FastBFS(g);
        wBfs = new FastBFS(g);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        vBfs.startBfsInLockstep(v);
        wBfs.startBfsInLockstep(w);
        return ancestor().length;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        vBfs.startBfsInLockstep(v);
        wBfs.startBfsInLockstep(w);
        return ancestor().ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        vBfs.startBfsInLockstep(v);
        wBfs.startBfsInLockstep(w);
        return ancestor().length;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        vBfs.startBfsInLockstep(v);
        wBfs.startBfsInLockstep(w);
        return ancestor().ancestor;
    }

    private Ancestor ancestor() {
        int shortestPath = Integer.MAX_VALUE;
        int ancestor = -1;

        int lastV = -1;
        int lastW = -1;

        do {
            lastV = vBfs.makeStep();
            lastW = wBfs.makeStep();

            int shortestCandidate = getDistance(lastV, wBfs);
            if (shortestCandidate != -1 && shortestCandidate < shortestPath) {
                ancestor = lastV;
                shortestPath = shortestCandidate;
            }

            shortestCandidate = getDistance(lastW, vBfs);
            if (shortestCandidate != -1 && shortestCandidate < shortestPath) {
                ancestor = lastW;
                shortestPath = shortestCandidate;
            }

            if (vBfs.getCurrentDistance() > shortestPath) {
                vBfs.terminate();
            }

            if (wBfs.getCurrentDistance() > shortestPath) {
                wBfs.terminate();
            }
        } while (lastV != -1 || lastW != -1);

        return ancestor == -1
               ? new Ancestor(-1, -1)
               : new Ancestor(ancestor, shortestPath);
    }

    private int getDistance(int v, FastBFS bfs) {
        return v != -1 && bfs.hasPathTo(v)
               ? vBfs.distanceTo(v) + wBfs.distanceTo(v)
               : -1;
    }

    private int

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
