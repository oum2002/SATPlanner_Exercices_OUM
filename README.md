# SAT Planner - Planificateur SAT en Java avec PDDL4J

Ce projet est une implémentation d’un planificateur basé sur la réduction à la satisfiabilité propositionnelle (SAT), utilisant la bibliothèque [PDDL4J](http://pddl4j.imag.fr) pour le traitement des fichiers PDDL et [SAT4J](https://www.sat4j.org/) comme solveur SAT.

## Objectif
Ce projet a pour objectif de :

Implémenter un planificateur SAT en Java utilisant la bibliothèque PDDL4J pour lire et instancier des problèmes PDDL.

Traduire automatiquement le problème de planification en formules logiques en forme normale conjonctive (CNF).

Résoudre ce problème à l’aide du solveur SAT4J.

Appliquer cette méthode à un cas simple de robot :

Un robot doit se déplacer d’une pièce à une autre (de R1 à R2). Il peut exécuter des actions de type move(from, to) avec pour précondition qu’il se trouve dans la pièce from, et pour effet qu’il se retrouve dans to. Le but est de générer un plan valide atteignant l’état final souhaité.

## ⚙️ Compilation

Ouvrez un terminal à la racine du projet et exécutez :

```bash
javac -d classes -cp "lib/*" src/fr/uga/pddl4j/tutorial/SATPlanner/*.java
```
## Execution
```bash
java -cp "classes:lib/*" fr.uga.pddl4j.tutorial.SATPlanner.SATPlanner pddl/domain.pddl pddl/problem.pddl

```
