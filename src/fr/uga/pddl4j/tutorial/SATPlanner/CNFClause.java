package fr.uga.pddl4j.tutorial.SATPlanner;

import java.util.*;

public class CNFClause {
    private final List<Integer> literals;

    public CNFClause(Integer... literals) {
        this.literals = new ArrayList<>(Arrays.asList(literals));
    }

    public List<Integer> getLiterals() {
        return literals;
    }

    public void addLiteral(int lit) {
        literals.add(lit);
    }

    @Override
    public String toString() {
        return literals.toString();
    }
}
