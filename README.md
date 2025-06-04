# SAT Planner - Planificateur SAT en Java avec PDDL4J

Ce projet est une implémentation d’un planificateur basé sur la réduction à la satisfiabilité propositionnelle (SAT), utilisant la bibliothèque [PDDL4J](http://pddl4j.imag.fr) pour le traitement des fichiers PDDL et [SAT4J](https://www.sat4j.org/) comme solveur SAT.

## Objectif

- Charger un domaine et un problème décrits en PDDL.
- Instancier le problème avec PDDL4J.
- Traduire le problème en une formule SAT (CNF).
- Résoudre cette formule avec SAT4J.
- Extraire et afficher un plan valide.

##Prérequis

- Java 8 ou supérieur
- [PDDL4J](http://pddl4j.imag.fr/download.html) (`pddl4j-4.0.0.jar`)
- [SAT4J](https://www.sat4j.org/download.html) (`sat4j-sat.jar`)

Placez les fichiers JAR dans le dossier `lib/`.

## ⚙️ Compilation

Ouvrez un terminal à la racine du projet et exécutez :

```bash
javac -d classes -cp "lib/*" src/fr/uga/pddl4j/tutorial/SATPlanner/*.java
```
## Execution
```bash
java -cp "classes:lib/*" fr.uga.pddl4j.tutorial.SATPlanner.SATPlanner pddl/domain.pddl pddl/problem.pddl

```
