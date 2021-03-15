import edu.princeton.cs.algs4.Digraph;

import java.util.Iterator;

public class ArrayDigraph {
    private final int[][] graph;
    private final int vertexCount;
    private final int edgesCount;

    public ArrayDigraph(Digraph d) {
        vertexCount = d.V();
        graph = new int[vertexCount][];

        int eCounter = 0;
        for (int v = 0; v < vertexCount; ++v) {
            int adjCounter = 0;

            Iterator<Integer> it = d.adj(v).iterator();
            while (it.hasNext()) {
                ++adjCounter;
                ++eCounter;
                it.next();
            }

            graph[v] = new int[adjCounter];
            it = d.adj(v).iterator();
            while (it.hasNext()) {
                graph[v][adjCounter-- - 1] = it.next();
            }
        }

        edgesCount = eCounter;
    }

    public int[] adj(int v) {
        return graph[v];
    }

    public int V() {
        return vertexCount;
    }

    public int E() {
        return edgesCount;
    }
}
