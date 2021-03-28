import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseballElimination {
    private class Team {
        public final int number;
        public final String name;
        public final int wins;
        public final int losses;
        public final int totalRemaining;
        public final int remaining;

        public Team(int number, String name, int wins, int losses, int totalRemaining,
                    int remaining) {
            this.number = number;
            this.name = name;
            this.wins = wins;
            this.losses = losses;
            this.totalRemaining = totalRemaining;
            this.remaining = remaining;
        }
    }

    private class Elimination {
        private static final int INVALID_TEAM_NO = -1;

        public int teamNumber;
        public Bag<String> certificate;
        boolean isEliminated;

        public Elimination() {
            reset();
        }

        public boolean isCached(Team team) {
            return team.number == teamNumber;
        }

        public void reset() {
            teamNumber = INVALID_TEAM_NO;
            certificate = new Bag<>();
            isEliminated = false;
        }
    }

    private final HashMap<String, Team> teams = new HashMap<>();
    private final int[][] remainingGames;
    private final Elimination lastElimination = new Elimination();

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Input file is null");
        }

        In file = new In(filename);

        int teamCount = Integer.parseInt(file.readLine());
        remainingGames = new int[teamCount][teamCount];
        int teamCounter = 0;

        while (file.hasNextLine()) {
            final String[] tokens = file.readLine().split("\\s+");

            final int teamNo = teamCounter++;
            int tokenCounter = tokens[0].equals("") ? 1 : 0;

            final String name = tokens[tokenCounter++];
            final int wins = Integer.parseInt(tokens[tokenCounter++]);
            final int losses = Integer.parseInt(tokens[tokenCounter++]);
            final int totalRemaining = Integer.parseInt(tokens[tokenCounter++]);
            int remaining = 0;

            for (int iToken = tokenCounter; iToken < tokens.length; ++iToken) {
                final int anotherTeamNo = iToken - tokenCounter;
                if (anotherTeamNo == teamNo) {
                    continue;
                }

                final int gamesLeft = Integer.parseInt(tokens[iToken]);
                remaining += gamesLeft;
                remainingGames[teamNo][anotherTeamNo] = gamesLeft;
            }

            Team team = new Team(teamNo, name, wins, losses, totalRemaining, remaining);
            teams.put(name, team);
        }
    }

    // number of teams
    public int numberOfTeams() {
        return teams.size();
    }

    // all teams
    public Iterable<String> teams() {
        return teams.keySet();
    }

    // number of wins for given team
    public int wins(String team) {
        return getTeam(team).wins;
    }

    // number of losses for given team
    public int losses(String team) {
        return getTeam(team).losses;
    }

    // number of remaining games for given team
    public int remaining(String team) {
        return getTeam(team).totalRemaining;
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        final Team t1 = getTeam(team1);
        final Team t2 = getTeam(team2);
        return remainingGames[t1.number][t2.number];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        Elimination e = getElimination(team);
        return e.isEliminated;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        Elimination e = getElimination(team);
        return e.isEliminated ? e.certificate : null;
    }

    private Team getTeam(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Team name is null");
        }

        Team team = teams.get(name);
        if (team == null) {
            throw new IllegalArgumentException("Team '" + name + "' is unknown");
        }

        return team;
    }

    private Elimination getElimination(String teamName) {
        Team t = getTeam(teamName);
        if (!lastElimination.isCached(t)) {
            lastElimination.reset();
            updateElimination(t);
        }
        return lastElimination;
    }

    private void updateElimination(Team teamToBeEliminated) {
        lastElimination.teamNumber = teamToBeEliminated.number;
        final int maxPossibleWins = teamToBeEliminated.wins + teamToBeEliminated.totalRemaining;

        // StdOut.println("Trying to eliminate " + teamToBeEliminated.name + ", number "
        //                        + teamToBeEliminated.number);
        ArrayList<Team> otherTeams = new ArrayList<>();
        otherTeams.ensureCapacity(teams.size() / 2);

        int gamesAmongOtherTeams = 0;
        int teamGames = 0;

        // Check for trivial elimination
        for (Team team : teams.values()) {
            if (team == teamToBeEliminated) {
                continue;
            }

            if (team.wins > maxPossibleWins) {
                lastElimination.isEliminated = true;
                lastElimination.certificate.add(team.name);
                // StdOut.println("Eliminated trivially by " + team.name);
                return;
            }

            final int remainingGamesWithOthers = team.remaining
                    - remainingGames[team.number][teamToBeEliminated.number];
            if (remainingGamesWithOthers > 0) {
                for (int i = 0; i < teams.size(); ++i) {
                    if (i != teamToBeEliminated.number && remainingGames[team.number][i] > 0) {
                        ++teamGames;
                    }
                }
                gamesAmongOtherTeams += remainingGamesWithOthers;
                otherTeams.add(team);
            }
        }


        // Each game is counted twice
        teamGames /= 2;
        gamesAmongOtherTeams /= 2;
        // Additional two vertecies for sink and target
        final int vertexCount = otherTeams.size() + teamGames + 2;
        final int sinkVertexIndex = vertexCount - 2;
        final int targetVertexIndex = vertexCount - 1;

        // StdOut.println(
        //         String.format("G params: V: %d, sIndex: %d, tIndex: %d, gaot: %d", vertexCount,
        //                       sinkVertexIndex, targetVertexIndex, gamesAmongOtherTeams));

        FlowNetwork flowNetwork = new FlowNetwork(vertexCount);
        int gameNextVertexIndex = otherTeams.size();

        for (int iTeam1 = 0; iTeam1 < otherTeams.size(); ++iTeam1) {
            final Team team1 = otherTeams.get(iTeam1);
            final double targetEdgeFlow = maxPossibleWins - team1.wins;

            flowNetwork.addEdge(new FlowEdge(iTeam1, targetVertexIndex, targetEdgeFlow));

            for (int iTeam2 = iTeam1 + 1; iTeam2 < otherTeams.size(); ++iTeam2) {
                final Team team2 = otherTeams.get(iTeam2);
                final int gamesLeft = remainingGames[team1.number][team2.number];
                // System.out.println(String.format("Games left between %d and %d is %d", team1Number,
                //                                  team2Number, gamesLeft));

                if (gamesLeft != 0) {
                    final int gameNodeVertex = gameNextVertexIndex++;
                    // Edge from sink to game between team1 and team2
                    flowNetwork.addEdge(new FlowEdge(sinkVertexIndex, gameNodeVertex, gamesLeft));
                    // First possible outcome
                    flowNetwork.addEdge(
                            new FlowEdge(gameNodeVertex, iTeam1, Double.POSITIVE_INFINITY));
                    // Second possible outcome
                    flowNetwork.addEdge(
                            new FlowEdge(gameNodeVertex, iTeam2, Double.POSITIVE_INFINITY));
                }
            }
        }

        FordFulkerson ff = new FordFulkerson(flowNetwork, sinkVertexIndex, targetVertexIndex);

        // StdOut.println("FF value: " + ff.value() + ", games among teams: " + gamesAmongOtherTeams);
        if (ff.value() == gamesAmongOtherTeams) { // All games are distributed
            return; // Team is not eliminated
        }

        lastElimination.isEliminated = true;
        for (int iTeam = 0; iTeam < otherTeams.size(); ++iTeam) {
            final Team team = otherTeams.get(iTeam);
            if (ff.inCut(iTeam)) {
                lastElimination.certificate.add(team.name);
            }
        }
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
