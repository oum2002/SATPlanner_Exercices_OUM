package fr.uga.pddl4j.tutorial.SATPlanner;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.HashMap;
import java.util.Map;

public class SATPlanner {

    private final Encoder encoder;
    private final ISolver solver;

    // Map de la variable SAT vers l’action (indexée à partir de 1)
    private final Map<Integer, String> varToAction;

    public SATPlanner(String domainPath, String problemPath) throws Exception {
        this.encoder = new Encoder(domainPath, problemPath);
        this.solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // timeout 1h

        this.varToAction = new HashMap<>();

        this.encodeProblemToSAT();
    }

    private void encodeProblemToSAT() throws ContradictionException {
        int varIndex = 1;
        for (var action : encoder.getProblem().getActions()) {
            // On associe chaque action à une variable SAT
            varToAction.put(varIndex, encoder.getProblem().toString(action));

            // Exemple simplifié : on ajoute la clause (action_var)
            // ce qui signifie que cette action est possible (en vrai, il faut plus complexe)
            solver.addClause(new VecInt(new int[]{varIndex}));

            varIndex++;
        }
    }

    public void solve() {
        try {
            if (solver.isSatisfiable()) {
                int[] model = solver.model();
                System.out.println("Solution SAT trouvée :");
                for (int var : model) {
                    if (var > 0) {  // variable vraie
                        String actionName = varToAction.get(var);
                        if (actionName != null) {
                            System.out.println(" - " + actionName);
                        }
                    }
                }
            } else {
                System.out.println("Problème insatisfaisable.");
            }
        } catch (TimeoutException e) {
            System.err.println("Timeout du solveur SAT.");
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java SatPlanner <domain-file> <problem-file>");
            System.exit(1);
        }

        SATPlanner planner = new SATPlanner(args[0], args[1]);
        planner.solve();
    }
}
