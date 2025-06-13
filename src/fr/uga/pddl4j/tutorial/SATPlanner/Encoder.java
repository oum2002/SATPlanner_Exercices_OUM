package fr.uga.pddl4j.tutorial.SATPlanner;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;

public class Encoder {

    private final Problem problem;
    private final DefaultParsedProblem parsedProblem;

    public Encoder(String domainPath, String problemPath) throws Exception {
        Parser parser = new Parser();
        this.parsedProblem = parser.parse(domainPath, problemPath);

        if (this.parsedProblem == null) {
            throw new RuntimeException("Parsing failed.");
        }

        this.problem = new DefaultProblem(this.parsedProblem);
        this.problem.instantiate();
    }

    public Problem getProblem() {
        return this.problem;
    }

    public DefaultParsedProblem getParsedProblem() {
        return this.parsedProblem;
    }
}
