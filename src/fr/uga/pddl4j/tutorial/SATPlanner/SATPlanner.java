package fr.uga.pddl4j.tutorial.SATPlanner;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import fr.uga.pddl4j.problem.operator.AbstractOperator;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.InitialState;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.parser.DefaultParsedProblem;

import java.util.*;

public class SATPlanner {
    private final Encoder encoder;
    private final ISolver solver;
    private final int maxSteps;
    
    private final Map<String, Integer> propToIndex;
    private final Map<String, Integer> actionToIndex;
    private final Map<Integer, String> varToDescription;
    
    private int nextVarId = 1;
    
    public SATPlanner(String domainPath, String problemPath, int maxSteps) throws Exception {
        this.encoder = new Encoder(domainPath, problemPath);
        this.solver = SolverFactory.newDefault();
        this.maxSteps = maxSteps;
        solver.setTimeout(3600);
        
        this.propToIndex = new HashMap<>();
        this.actionToIndex = new HashMap<>();
        this.varToDescription = new HashMap<>();
        
        this.encodeProblemToSAT();
    }
    
    private void encodeProblemToSAT() throws ContradictionException {
        createMappings();
        encodeInitialState();
        encodeGoalState();
        encodeActions();
        encodeFrameAxioms();
        encodeActionExclusivity();
    }
    
    private void createMappings() {
        int propIndex = 0;
        for (int i = 0; i < encoder.getProblem().getFluents().size(); i++) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            propToIndex.put(predicate, propIndex++);
        }
        
        int actionIndex = 0;
        for (Action action : encoder.getProblem().getActions()) {
            String actionName = encoder.getProblem().toString(action);
            actionToIndex.put(actionName, actionIndex++);
        }
    }
    
    private int getPropVar(String proposition, int timeStep) {
        int propIndex = propToIndex.get(proposition);
        int varId = 1 + propIndex * (maxSteps + 1) + timeStep;
        
        if (!varToDescription.containsKey(varId)) {
            varToDescription.put(varId, proposition + "@" + timeStep);
        }
        
        return varId;
    }
    
    private int getActionVar(String action, int timeStep) {
        int actionIndex = actionToIndex.get(action);
        int baseVarId = 1 + propToIndex.size() * (maxSteps + 1);
        int varId = baseVarId + actionIndex * maxSteps + timeStep;
        
        if (!varToDescription.containsKey(varId)) {
            varToDescription.put(varId, "action_" + action + "@" + timeStep);
        }
        
        return varId;
    }
    
    private void encodeInitialState() throws ContradictionException {
        InitialState initialState = encoder.getProblem().getInitialState();
        
        BitVector positiveFacts = initialState.getPositiveFluents();
        for (int i = positiveFacts.nextSetBit(0); i >= 0; i = positiveFacts.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int var = getPropVar(predicate, 0);
            solver.addClause(new VecInt(new int[]{var}));
        }
        
        for (String proposition : propToIndex.keySet()) {
            if (proposition.startsWith("(at r1")) {
                boolean isPositive = false;
                for (int i = positiveFacts.nextSetBit(0); i >= 0; i = positiveFacts.nextSetBit(i + 1)) {
                    String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
                    if (predicate.equals(proposition)) {
                        isPositive = true;
                        break;
                    }
                }
                
                if (!isPositive) {
                    int var = getPropVar(proposition, 0);
                    solver.addClause(new VecInt(new int[]{-var}));
                }
            }
        }
        
        BitVector negativeFacts = initialState.getNegativeFluents();
        for (int i = negativeFacts.nextSetBit(0); i >= 0; i = negativeFacts.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int var = getPropVar(predicate, 0);
            solver.addClause(new VecInt(new int[]{-var}));
        }
    }
    
    private void encodeGoalState() throws ContradictionException {
        Condition goal = encoder.getProblem().getGoal();

        List<Integer> goalLits = new ArrayList<>();

        for (int i = goal.getPositiveFluents().nextSetBit(0); i >= 0; i = goal.getPositiveFluents().nextSetBit(i + 1)) {
            int var = getPropVar(encoder.getProblem().toString(encoder.getProblem().getFluents().get(i)), maxSteps);
            goalLits.add(var);
        }

        for (int i = goal.getNegativeFluents().nextSetBit(0); i >= 0; i = goal.getNegativeFluents().nextSetBit(i + 1)) {
            int var = getPropVar(encoder.getProblem().toString(encoder.getProblem().getFluents().get(i)), maxSteps);
            goalLits.add(-var);
        }

        solver.addClause(new VecInt(goalLits.stream().mapToInt(Integer::intValue).toArray()));
    }
    
    private void encodeActions() throws ContradictionException {
        for (Action action : encoder.getProblem().getActions()) {
            String actionName = encoder.getProblem().toString(action);
            
            for (int t = 0; t < maxSteps; t++) {
                int actionVar = getActionVar(actionName, t);
                encodePreconditions(action, actionVar, t);
                encodeEffects(action, actionVar, t);
            }
        }
    }
    
    private void encodePreconditions(Action action, int actionVar, int timeStep) throws ContradictionException {
        BitVector positivePreconditions = action.getPrecondition().getPositiveFluents();
        BitVector negativePreconditions = action.getPrecondition().getNegativeFluents();
        
        for (int i = positivePreconditions.nextSetBit(0); i >= 0; i = positivePreconditions.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int precondVar = getPropVar(predicate, timeStep);
            solver.addClause(new VecInt(new int[]{-actionVar, precondVar}));
        }
        
        for (int i = negativePreconditions.nextSetBit(0); i >= 0; i = negativePreconditions.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int precondVar = getPropVar(predicate, timeStep);
            solver.addClause(new VecInt(new int[]{-actionVar, -precondVar}));
        }
    }
    
    private void encodeEffects(Action action, int actionVar, int timeStep) throws ContradictionException {
        if (timeStep >= maxSteps) return;
        
        BitVector positiveEffects = action.getUnconditionalEffect().getPositiveFluents();
        BitVector negativeEffects = action.getUnconditionalEffect().getNegativeFluents();
        
        for (int i = positiveEffects.nextSetBit(0); i >= 0; i = positiveEffects.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int effectVar = getPropVar(predicate, timeStep + 1);
            solver.addClause(new VecInt(new int[]{-actionVar, effectVar}));
        }
        
        for (int i = negativeEffects.nextSetBit(0); i >= 0; i = negativeEffects.nextSetBit(i + 1)) {
            String predicate = encoder.getProblem().toString(encoder.getProblem().getFluents().get(i));
            int effectVar = getPropVar(predicate, timeStep + 1);
            solver.addClause(new VecInt(new int[]{-actionVar, -effectVar}));
        }
    }
    
    private void encodeFrameAxioms() throws ContradictionException {
        for (String proposition : propToIndex.keySet()) {
            for (int t = 0; t < maxSteps; t++) {
                int propCurrentVar = getPropVar(proposition, t);
                int propNextVar = getPropVar(proposition, t + 1);
                
                List<Integer> addingActions = new ArrayList<>();
                List<Integer> deletingActions = new ArrayList<>();
                
                for (Action action : encoder.getProblem().getActions()) {
                    String actionName = encoder.getProblem().toString(action);
                    int actionVar = getActionVar(actionName, t);
                    
                    if (actionAdds(action, proposition)) {
                        addingActions.add(actionVar);
                    }
                    
                    if (actionDeletes(action, proposition)) {
                        deletingActions.add(actionVar);
                    }
                }
                
                if (!addingActions.isEmpty()) {
                    VecInt clause = new VecInt();
                    clause.push(propCurrentVar);
                    clause.push(-propNextVar);
                    for (int addingAction : addingActions) {
                        clause.push(addingAction);
                    }
                    solver.addClause(clause);
                }
                
                if (!deletingActions.isEmpty()) {
                    VecInt clause = new VecInt();
                    clause.push(-propCurrentVar);
                    clause.push(propNextVar);
                    for (int deletingAction : deletingActions) {
                        clause.push(deletingAction);
                    }
                    solver.addClause(clause);
                }
                
                if (proposition.startsWith("(at r1")) {
                    for (String otherProp : propToIndex.keySet()) {
                        if (!otherProp.equals(proposition) && otherProp.startsWith("(at r1")) {
                            int otherPropVar = getPropVar(otherProp, t + 1);
                            solver.addClause(new VecInt(new int[]{-propNextVar, -otherPropVar}));
                        }
                    }
                }
            }
        }
    }
    
    private boolean actionAdds(Action action, String proposition) {
        int propIndex = propToIndex.get(proposition);
        return action.getUnconditionalEffect().getPositiveFluents().get(propIndex);
    }
    
    private boolean actionDeletes(Action action, String proposition) {
        int propIndex = propToIndex.get(proposition);
        return action.getUnconditionalEffect().getNegativeFluents().get(propIndex);
    }
    
    private void encodeActionExclusivity() throws ContradictionException {
        for (int t = 0; t < maxSteps; t++) {
            List<Integer> actionVars = new ArrayList<>();
            for (String actionName : actionToIndex.keySet()) {
                actionVars.add(getActionVar(actionName, t));
            }
            
            for (int i = 0; i < actionVars.size(); i++) {
                for (int j = i + 1; j < actionVars.size(); j++) {
                    solver.addClause(new VecInt(new int[]{-actionVars.get(i), -actionVars.get(j)}));
                }
            }
        }
    }
    
    private String getCleanActionName(String fullActionName) {
        if (fullActionName.contains("move") && fullActionName.contains("?X0 - robot : r1")) {
            String[] lines = fullActionName.split("\n");
            String from = null, to = null;
            
            for (String line : lines) {
                if (line.contains("?X1 - location : ")) {
                    from = line.substring(line.indexOf(": ") + 2).trim();
                } else if (line.contains("?X2 - location : ")) {
                    to = line.substring(line.indexOf(": ") + 2).trim();
                }
            }
            
            if (from != null && to != null) {
                return "move(r1, " + from + ", " + to + ")";
            }
        }
        return fullActionName;
    }
    
    public void solve() {
        try {
            System.out.println("Propositions: " + propToIndex.size());
            System.out.println("Actions: " + actionToIndex.size());
            System.out.println("Étapes max: " + maxSteps);
            System.out.println("Variables SAT: " + solver.nVars());
            System.out.println("Contraintes: " + solver.nConstraints());
            
            propToIndex.entrySet().stream().limit(5).forEach(e -> 
                System.out.println("  " + e.getKey() + " -> " + e.getValue()));
            
            actionToIndex.entrySet().stream().limit(5).forEach(e -> 
                System.out.println("  " + e.getKey() + " -> " + e.getValue()));
            
            if (solver.isSatisfiable()) {
                System.out.println("Solution SAT trouvée!");
                
                int[] model = solver.model();
                Set<Integer> trueVars = new HashSet<>();
                for (int v : model) {
                    if (v > 0) trueVars.add(v);
                }
                
                System.out.println("Nombre de variables vraies: " + trueVars.size());
                
                for (int var : trueVars) {
                    String desc = varToDescription.get(var);
                    if (desc != null) {
                        System.out.println("  " + var + ": " + desc);
                    }
                }
                
                Map<Integer, String> planMap = new TreeMap<>();
                
                for (Map.Entry<String, Integer> entry : actionToIndex.entrySet()) {
                    String actionName = entry.getKey();
                    int actionIdx = entry.getValue();
                    
                    for (int t = 0; t < maxSteps; t++) {
                        int actionVar = getActionVar(actionName, t);
                        
                        if (trueVars.contains(actionVar)) {
                            planMap.put(t, actionName);
                        }
                    }
                }

                if (planMap.isEmpty()) {
                    System.out.println("Plan vide : peut-être que l'état initial satisfait déjà le but.");
                } else {
                    System.out.println("Plan trouvé :");
                    for (Map.Entry<Integer, String> step : planMap.entrySet()) {
                        String cleanAction = getCleanActionName(step.getValue());
                        System.out.println(" - Étape " + step.getKey() + ": " + cleanAction);
                    }
                }
                
            } else {
                System.out.println("Problème insatisfaisable avec maxSteps = " + maxSteps);
            }
            
        } catch (TimeoutException e) {
            System.err.println("Timeout du solveur SAT.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la résolution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java SATPlanner <domain-file> <problem-file> [max-steps]");
            System.exit(1);
        }
        
        int maxSteps = args.length == 3 ? Integer.parseInt(args[2]) : 20;
        SATPlanner planner = new SATPlanner(args[0], args[1], maxSteps);
        planner.solve();
    }
}