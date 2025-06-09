import os
import subprocess
import time
import pandas as pd
import matplotlib.pyplot as plt

# Configurations
domain = "Robot"
domain_file = "/workspaces/SATPlanner_Exercices_OUM/pddl/domain.pddl"

problems_dir = "/workspaces/SATPlanner_Exercices_OUM/pddl"
# Liste explicite des problèmes à tester
problem_files = ["p01.pddl", "p02.pddl", "p03.pddl"]  # ajoute autant que tu veux

sat_command = "java -cp 'classes:lib/*' fr.uga.pddl4j.tutorial.SATPlanner.SATPlanner"
hsp_command = "java -cp lib/pddl4j-4.0.0.jar fr.uga.pddl4j.planners.statespace.HSP"

results = []

for problem_file_name in problem_files:
    problem_file = os.path.join(problems_dir, problem_file_name)
    problem_name = os.path.splitext(problem_file_name)[0]

    print(f"Traitement de {problem_name}...")

    # SAT Planner
    start = time.time()
    sat_proc = subprocess.run(f"{sat_command} {domain_file} {problem_file}", shell=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    end = time.time()
    sat_time = end - start
    sat_output = sat_proc.stdout.splitlines()

    # Extraire le plan (filtre simple, adapte si besoin)
    sat_plan = [line for line in sat_output if "move" in line or "unstack" in line or "pick" in line]
    sat_makespan = len(sat_plan)

    # HSP Planner
    start = time.time()
    hsp_proc = subprocess.run(f"{hsp_command} -o {domain_file} -f {problem_file}", shell=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    end = time.time()
    hsp_time = end - start
    hsp_output = hsp_proc.stdout.splitlines()

    # Extraire plan HSP (adapter selon sortie HSP)
    hsp_plan = [line for line in hsp_output if "step" in line.lower()]
    hsp_makespan = len(hsp_plan)

    results.append({
        "domain": domain,
        "problem": problem_name,
        "SAT_time": sat_time,
        "SAT_makespan": sat_makespan,
        "HSP_time": hsp_time,
        "HSP_makespan": hsp_makespan
    })

# Analyse des résultats
df = pd.DataFrame(results)

df = df.sort_values(by="problem")

plt.figure(figsize=(10, 5))
plt.plot(df["problem"], df["SAT_time"], label="SATPlanner", marker="o")
plt.plot(df["problem"], df["HSP_time"], label="HSP", marker="s")
plt.title(f"{domain} - Temps d'exécution")
plt.xlabel("Problème")
plt.ylabel("Temps (s)")
plt.legend()
plt.grid()
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig(f"{domain}_runtime.png")

plt.figure(figsize=(10, 5))
plt.plot(df["problem"], df["SAT_makespan"], label="SATPlanner", marker="o")
plt.plot(df["problem"], df["HSP_makespan"], label="HSP", marker="s")
plt.title(f"{domain} - Longueur du plan")
plt.xlabel("Problème")
plt.ylabel("Makespan")
plt.legend()
plt.grid()
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig(f"{domain}_makespan.png")

print("✅ Comparaison terminée. Graphiques sauvegardés.")
