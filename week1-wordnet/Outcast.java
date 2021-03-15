import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet net;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        net = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        if (nouns == null) {
            throw new IllegalArgumentException("Nouns are null");
        }

        if (nouns.length < 2) {
            throw new IllegalArgumentException("At least 2 nouns must be provided for outcast");
        }

        int maxDistance = Integer.MIN_VALUE;
        String outcast = null;

        for (int i = 0; i < nouns.length; ++i) {
            int distance = 0;
            String first = nouns[i];
            for (int j = 0; j < nouns.length; ++j) {
                if (i != j) {
                    int d = net.distance(first, nouns[j]);
                    distance += d;
                }
            }

            if (distance > maxDistance) {
                maxDistance = distance;
                outcast = first;
            }
        }

        return outcast;
    }


    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
