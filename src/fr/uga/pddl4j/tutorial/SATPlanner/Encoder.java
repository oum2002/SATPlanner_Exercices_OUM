package fr.uga.pddl4j.tutorial.SATPlanner;

import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;

public class Encoder {

    private final Problem problem;

    public Encoder(String domainPath, String problemPath) throws Exception {
        // Étape 1 : parse les fichiers
        Parser parser = new Parser();
        DefaultParsedProblem parsedProblem = parser.parse(domainPath, problemPath);

        if (parsedProblem == null) {
            throw new RuntimeException("Parsing failed.");
        }

        // Étape 2 : instanciation (grounding)
        this.problem = new DefaultProblem(parsedProblem);
        this.problem.instantiate();
    }

    public Problem getProblem() {
        return this.problem;
    }

    
    
}
