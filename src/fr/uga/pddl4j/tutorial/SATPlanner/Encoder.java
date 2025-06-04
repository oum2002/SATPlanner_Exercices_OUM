package fr.uga.pddl4j.tutorial.SATPlanner;

import java.util.*;

public class Encoder {
    private int varCounter = 1;
    private final Map<String, Integer> varMap = new HashMap<>();

    public int getOrCreateVar(String name) {
        return varMap.computeIfAbsent(name, k -> varCounter++);
    }

    public List<CNFClause> encode(int horizon) {
        List<CNFClause> clauses = new ArrayList<>();

        // Exemple simple : le robot commence en R1, doit aller en R2 à l'horizon <= 3
        // Actions : move(R1, R2), move(R2, R1)
        for (int t = 0; t <= horizon; t++) {
            int atR1 = getOrCreateVar("at(R1,t" + t + ")");
            int atR2 = getOrCreateVar("at(R2,t" + t + ")");
            clauses.add(new CNFClause(atR1, atR2)); // le robot est quelque part
            clauses.add(new CNFClause(-atR1, -atR2)); // pas dans les deux
        }

        for (int t = 0; t < horizon; t++) {
            int moveR1R2 = getOrCreateVar("move(R1,R2,t" + t + ")");
            int moveR2R1 = getOrCreateVar("move(R2,R1,t" + t + ")");

            int atR1_t = getOrCreateVar("at(R1,t" + t + ")");
            int atR2_t = getOrCreateVar("at(R2,t" + t + ")");
            int atR2_tp1 = getOrCreateVar("at(R2,t" + (t+1) + ")");
            int atR1_tp1 = getOrCreateVar("at(R1,t" + (t+1) + ")");

            // Précondition : pour faire move(R1,R2), il faut être à R1
            clauses.add(new CNFClause(-moveR1R2, atR1_t));
            clauses.add(new CNFClause(-moveR2R1, atR2_t));

            // Effets
            clauses.add(new CNFClause(-moveR1R2, atR2_tp1)); // si move, alors à R2
            clauses.add(new CNFClause(-moveR2R1, atR1_tp1));

            // Persistance : pas de move -> état inchangé
        }

        // État initial : robot en R1
        clauses.add(new CNFClause(getOrCreateVar("at(R1,t0)")));
        clauses.add(new CNFClause(-getOrCreateVar("at(R2,t0)")));

        // Objectif : robot en R2 à t<=horizon
        List<Integer> goal = new ArrayList<>();
        for (int t = 0; t <= horizon; t++) {
            goal.add(getOrCreateVar("at(R2,t" + t + ")"));
        }
        clauses.add(new CNFClause(goal.toArray(new Integer[0])));

        return clauses;
    }

    public Map<String, Integer> getVarMap() {
        return varMap;
    }
}
