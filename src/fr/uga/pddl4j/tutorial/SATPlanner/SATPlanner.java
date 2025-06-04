package fr.uga.pddl4j.tutorial.SATPlanner;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

public class SATPlanner {

    public static void main(String[] args) throws ContradictionException, TimeoutException {
        int horizon = 3;

        Encoder encoder = new Encoder();
        List<CNFClause> clauses = encoder.encode(horizon);

        ISolver solver = SolverFactory.newDefault();
        solver.newVar(1000); // nombre max de variables
        solver.setExpectedNumberOfClauses(clauses.size());

        for (CNFClause clause : clauses) {
            int[] lits = clause.getLiterals().stream().mapToInt(i -> i).toArray();
            solver.addClause(new VecInt(lits));
        }

        if (solver.isSatisfiable()) {
            int[] model = solver.model();
            Map<Integer, String> invVarMap = new HashMap<>();
            encoder.getVarMap().forEach((k, v) -> invVarMap.put(v, k));

            System.out.println("Plan trouvé :");
            Arrays.stream(model)
                    .filter(i -> i > 0)
                    .mapToObj(invVarMap::get)
                    .filter(name -> name.startsWith("move"))
                    .sorted()
                    .forEach(System.out::println);
        } else {
            System.out.println("Aucun plan trouvé jusqu'à l'horizon " + horizon);
        }
    }
}

