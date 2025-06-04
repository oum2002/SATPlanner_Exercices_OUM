package fr.uga.pddl4j.tutorial.satplanner;

public class CNFClause {
    private int[] literals;

    public CNFClause(int[] literals) {
        this.literals = literals;
    }

    public int[] getLiterals() {
        return literals;
    }
}
