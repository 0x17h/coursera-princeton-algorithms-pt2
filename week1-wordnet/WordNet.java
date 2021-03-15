import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;

import java.util.HashMap;

public class WordNet {
    // Noun -> vertex ids - one nout might be assigned to several vertexes
    private final HashMap<String, Bag<Integer>> nouns = new HashMap<String, Bag<Integer>>();
    // Vertex id -> synset
    private final HashMap<Integer, String> synsets = new HashMap<Integer, String>();
    private final Digraph digraph;

    // constructor takes the name of the two input files
    public WordNet(String synsetsFileName, String hypernymsFileName) {
        if (synsetsFileName == null) {
            throw new IllegalArgumentException("File with synsets is null");
        }

        if (hypernymsFileName == null) {
            throw new IllegalArgumentException("File with hypernyms is null");
        }

        In sFile = new In(synsetsFileName);
        In hFile = new In(hypernymsFileName);

        loadSynsets(sFile);
        digraph = new Digraph(nouns.size());
        loadHypernyms(hFile);

        checkForCycles();
        checkThatOneRooted();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return nouns.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        return 0;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        return "";
    }

    private void loadSynsets(In file) {
        while (file.hasNextLine()) {
            String line = file.readLine();
            String[] tokens = line.split(",");
            final int vertexId = Integer.parseInt(tokens[0]);

            String synset = tokens[1];
            synsets.put(vertexId, synset);

            String[] nouns = tokens[1].split(" ");
            for (String noun : nouns) {
                Bag<Integer> vertexes = this.nouns.get(noun);
                if (vertexes == null) {
                    vertexes = new Bag<Integer>();
                    this.nouns.put(noun, vertexes);
                }

                vertexes.add(vertexId);
            }
        }
    }

    private void loadHypernyms(In file) {
        while (file.hasNextLine()) {
            String line = file.readLine();
            String[] tokens = line.split(",");
            if (tokens.length < 2) {
                continue;
            }

            final int v = Integer.parseInt(tokens[0]);
            for (int iEdge = 1; iEdge < tokens.length; ++iEdge) {
                digraph.addEdge(v, Integer.parseInt(tokens[iEdge]));
            }
        }
    }

    private void checkThatOneRooted() {
        int rootCount = 0;
        for (int v = 0; v < digraph.V(); ++v) {
            if (!digraph.adj(v).iterator().hasNext()) {
                if (++rootCount > 2) {
                    throw new IllegalArgumentException("Graph has more than 1 root");
                }
            }
        }

        if (rootCount != 1) {
            throw new IllegalArgumentException("Graph has no roots");
        }
    }

    private void checkForCycles() {
        CycleDetector detector = new CycleDetector(digraph);
        if (detector.hasCycle()) {
            throw new IllegalArgumentException("Graph is not DAG because it has cycle");
        }
    }

    private class CycleDetector {
        private static final byte VISITED = 1 << 0;
        private static final byte IN_STACK = 1 << 1;

        private final Digraph graph;
        private final byte[] visited;

        CycleDetector(Digraph d) {
            visited = new byte[d.V()];
            graph = d;
        }

        public boolean hasCycle() {
            for (int v = 0; v < graph.V(); ++v) {
                if (dfs(v)) {
                    return true;
                }
            }

            return false;
        }

        private boolean dfs(int vertex) {
            if ((visited[vertex] & VISITED) != 0) {
                return false;
            }

            visited[vertex] |= VISITED | IN_STACK;
            for (int w : graph.adj(vertex)) {
                if ((visited[w] & IN_STACK) != 0 || dfs(w)) {
                    return true;
                }
            }
            visited[vertex] &= ~IN_STACK;
            return false;
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {

    }
}
