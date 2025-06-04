package fr.uga.pddl4j.tutorial.SATPlanner;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.core.VecInt;

import java.util.*;

/**
 * Encoder est la classe qui transforme un problème de planification simple 
 * en une formule SAT sous forme CNF. 
 * Cette classe a été développée dans le cadre de mon projet personnel de planification avec PDDL4J.
 * Elle modélise un robot qui doit se déplacer entre deux positions R1 et R2 dans un horizon donné.
 * 
 * Auteur: oumkalthoum mhamdi
 * Date: 2025-06-04
 */
public class Encoder {

    private final Map<String, Integer> varMap = new HashMap<>();
    private int varCounter = 1;
    private ISolver solver;

    public ISolver getSolver() {
        return this.solver;
    }

    public Map<String, Integer> getVarMap() {
        return varMap;
    }

    private int getOrCreateVar(String name) {
        return varMap.computeIfAbsent(name, k -> varCounter++);
    }

    public void encode(int horizon) throws ContradictionException {
        solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1h timeout

        // Contrainte de position unique à chaque instant
        for (int t = 0; t <= horizon; t++) {
            int atR1 = getOrCreateVar("at(R1,t" + t + ")");
            int atR2 = getOrCreateVar("at(R2,t" + t + ")");

            // au moins une position
            solver.addClause(new VecInt(new int[]{atR1, atR2}));
            // pas deux à la fois
            solver.addClause(new VecInt(new int[]{-atR1, -atR2}));
        }

        // Actions, effets et persistance
        for (int t = 0; t < horizon; t++) {
            int moveR1R2 = getOrCreateVar("move(R1,R2,t" + t + ")");
            int moveR2R1 = getOrCreateVar("move(R2,R1,t" + t + ")");

            int atR1_t = getOrCreateVar("at(R1,t" + t + ")");
            int atR2_t = getOrCreateVar("at(R2,t" + t + ")");
            int atR1_tp1 = getOrCreateVar("at(R1,t" + (t + 1) + ")");
            int atR2_tp1 = getOrCreateVar("at(R2,t" + (t + 1) + ")");

            // préconditions
            solver.addClause(new VecInt(new int[]{-moveR1R2, atR1_t}));
            solver.addClause(new VecInt(new int[]{-moveR2R1, atR2_t}));

            // effets
            solver.addClause(new VecInt(new int[]{-moveR1R2, atR2_tp1}));
            solver.addClause(new VecInt(new int[]{-moveR2R1, atR1_tp1}));

            // exclusion mutuelle des actions
            solver.addClause(new VecInt(new int[]{-moveR1R2, -moveR2R1}));

            // persistance (frame axioms)
            solver.addClause(new VecInt(new int[]{-atR1_tp1, atR1_t, moveR2R1}));
            solver.addClause(new VecInt(new int[]{-atR2_tp1, atR2_t, moveR1R2}));
        }

        // état initial : R1
        solver.addClause(new VecInt(new int[]{getOrCreateVar("at(R1,t0)")}));
        solver.addClause(new VecInt(new int[]{-getOrCreateVar("at(R2,t0)")})); // pas à R2

        // objectif : être à R2 à un moment
        int[] goal = new int[horizon + 1];
        for (int t = 0; t <= horizon; t++) {
            goal[t] = getOrCreateVar("at(R2,t" + t + ")");
        }
        solver.addClause(new VecInt(goal));
    }
}