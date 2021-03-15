import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;

import java.util.HashMap;

public class WordNet {
    // Noun -> vertex ids - one noun might be assigned to several vertexes => might appear
    // in several synsets
    private final HashMap<String, Bag<Integer>> nouns = new HashMap<String, Bag<Integer>>();
    // Vertex id -> synset
    private final HashMap<Integer, String> synsets = new HashMap<Integer, String>();
    private final Digraph graph;
    private final SAP sap;

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

        int count = loadSynsets(sFile);
        graph = new Digraph(count);
        loadHypernyms(hFile);

        checkForCycles(graph);
        checkThatOneRooted(graph);

        sap = new SAP(graph);
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
        Iterable<Integer> a = getNoun(nounA);
        Iterable<Integer> b = getNoun(nounB);

        return sap.length(a, b);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        Iterable<Integer> a = getNoun(nounA);
        Iterable<Integer> b = getNoun(nounB);

        int ancestor = sap.ancestor(a, b);
        if (ancestor == -1) {
            throw new IllegalArgumentException(
                    "Words '" + nounA + "' and '" + nounB + "' don't have common ancestor");
        }

        return synsets.get(ancestor);
    }

    private int loadSynsets(In file) {
        int count = 0;
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

            ++count;
        }

        return count;
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
                graph.addEdge(v, Integer.parseInt(tokens[iEdge]));
            }
        }
    }

    private void checkThatOneRooted(Digraph g) {
        RootsDetector d = new RootsDetector(g);
        if (!d.hasOneRoot()) {
            throw new IllegalArgumentException("Provided graph doesn't have exactly one root");
        }
    }

    private void checkForCycles(Digraph g) {
        CycleDetector detector = new CycleDetector(g);
        if (detector.hasCycle(g)) {
            throw new IllegalArgumentException("Graph is not DAG because it has cycle");
        }
    }

    private Bag<Integer> getNoun(String noun) {
        if (noun == null) {
            throw new IllegalArgumentException("Noun can't be null");
        }
        Bag<Integer> v = nouns.get(noun);
        if (v == null) {
            throw new IllegalArgumentException("Noun '" + noun + "' is not a WordNet noun");
        }
        return v;
    }

    private class RootsDetector {
        Digraph graph;

        RootsDetector(Digraph g) {
            graph = g;
        }

        public boolean hasOneRoot() {
            int rootCount = 0;
            for (int v = 0; v < graph.V(); ++v) {
                if (!graph.adj(v).iterator().hasNext()) {
                    if (++rootCount > 1) {
                        return false;
                    }
                }
            }

            return rootCount == 1;
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

        public boolean hasCycle(Digraph g) {
            for (int v = 0; v < g.V(); ++v) {
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
        // Digraph d = new Digraph(new In(args[0]));
        // WordNet.checkForCycles(d);
        // WordNet.checkThatOneRooted(d);
        // new WordNet(args[0], args[1]);
    }
}
