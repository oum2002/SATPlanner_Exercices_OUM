package fr.uga.pddl4j.tutorial.satplanner;

import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.Action;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.FileNotFoundException;
import java.util.List;

public class SATPlanner {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SATPlanner <domain.pddl> <problem.pddl>");
            return;
        }

        try {
            // Parsing du domaine et du problème
            Parser parser = new Parser();
            DefaultParsedProblem parsedProblem = parser.parse(args[0], args[1]);

            if (!parser.getErrorManager().isEmpty()) {
                parser.getErrorManager().printAll();
                return;
            }

            // Instanciation du problème
            Problem problem = new DefaultProblem(parsedProblem);
            problem.instantiate();

            // Encodage en CNF
            Encoder encoder = new Encoder(problem);
            List<CNFClause> cnfClauses = encoder.encodeToCNF();

            // Résolution avec SAT4J
            ISolver solver = SolverFactory.newDefault();
            for (CNFClause clause : cnfClauses) {
                solver.addClause(new VecInt(clause.getLiterals()));
            }

            if (solver.isSatisfiable()) {
                int[] model = solver.model();
                // Extraction et affichage du plan
                encoder.extractPlan(model);
            } else {
                System.out.println("Aucun plan trouvé.");
            }

        } catch (FileNotFoundException e) {
            System.err.println("Fichier non trouvé: " + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("Temps de résolution dépassé.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
