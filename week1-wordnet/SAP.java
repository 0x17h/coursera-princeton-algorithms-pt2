import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private final Digraph graph;
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
        graph = g;
        vBfs = new FastBFS(graph);
        wBfs = new FastBFS(graph);
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

    public Ancestor ancestor() {
        int shortestPath = Integer.MAX_VALUE;
        int ancestor = -1;

        int lastV = -1;
        int lastW = -1;

        do {
            lastV = vBfs.makeStep();
            lastW = wBfs.makeStep();

            if (lastV != -1 && wBfs.hasPathTo(lastV)) {
                shortestPath = wBfs.distanceTo(lastV) + vBfs.distanceTo(lastV);
                ancestor = lastV;
            }
            else if (lastW != -1 && vBfs.hasPathTo(lastW)) {
                shortestPath = vBfs.distanceTo(lastW) + wBfs.distanceTo(lastW);
                ancestor = lastW;
            }

            int distance = vBfs.getCurrentDistance() + wBfs.getCurrentDistance();
            if (distance >= shortestPath) {
                break;
            }

        } while (lastV != -1 || lastW != -1);

        return ancestor == -1
               ? new Ancestor(-1, -1)
               : new Ancestor(ancestor, shortestPath);
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
