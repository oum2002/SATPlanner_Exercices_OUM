package fr.uga.pddl4j.tutorial.satplanner;

import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.Action;

import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private Problem problem;

    public Encoder(Problem problem) {
        this.problem = problem;
    }

    public List<CNFClause> encodeToCNF() {
        List<CNFClause> clauses = new ArrayList<>();

        // Exemple d'encodage : pour chaque action, créer une clause
        for (Action action : problem.getActions()) {
            // Encodage fictif pour illustration
            int[] literals = {/* encodage des préconditions et effets */};
            clauses.add(new CNFClause(literals));
        }

        return clauses;
    }

    public void extractPlan(int[] model) {
        // Extraction du plan à partir du modèle SAT
        // Affichage des actions sélectionnées
    }
}
