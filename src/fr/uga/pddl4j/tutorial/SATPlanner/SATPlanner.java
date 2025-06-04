package fr.uga.pddl4j.tutorial.SATPlanner;

import org.sat4j.specs.ISolver;

import java.util.*;


/**
 * Classe principale du projet de planification SAT.
 * Cette classe utilise l'encodeur pour générer la formule CNF,
 * puis interroge le solveur SAT pour trouver un plan réalisable.
 * 
 * Auteur: Oumkalthoum Mhamdi
 * Date: 2025-06-04
 */

public class SATPlanner {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: SATPlanner <domain.pddl> <problem.pddl>");
            return;
        }

        int horizon = 5; // horizon plus grand pour chercher plan plus long
        Encoder encoder = new Encoder();

        try {
            encoder.encode(horizon);
            ISolver solver = encoder.getSolver();

            Map<String, Integer> varMap = encoder.getVarMap();
            Map<Integer, String> invMap = new HashMap<>();
            varMap.forEach((k, v) -> invMap.put(v, k));

            if (solver.isSatisfiable()) {
                System.out.println("\nPlan trouvé :");
                int[] model = solver.model();

                List<String> plan = new ArrayList<>();

                for (int var : model) {
                    if (var > 0) {
                        String prop = invMap.get(var);
                        System.out.println("SAT var: " + prop);  // Affichage debug
                        if (prop != null && prop.contains("move")) {
                            plan.add(prop);
                        }
                    }
                }

                plan.sort(Comparator.comparingInt(SATPlanner::extractTime));
                for (String action : plan) {
                    System.out.println(action);
                }
            } else {
                System.out.println("Aucun plan trouvé.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int extractTime(String action) {
        try {
            int idx = action.lastIndexOf("t");
            return Integer.parseInt(action.substring(idx + 1));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
