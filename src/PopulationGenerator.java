import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class PopulationGenerator {

    private FileInterpreter fi;

    private int[][] population;

    private int generationsCount;
    private int maxGenerations;
    private float mutationRate;
    private LinkedList<Integer> mutationLines;
    private int convergenceCount;

    private HeuristicCalculator hc;

    public static enum Status {
        IN_PROGRESS, GENERATION_LIMIT_REACHED, PERFECT_SOLUTION_FOUND, CONVERGENCE_DETECTED
    }

    private Status status;

    public PopulationGenerator(FileInterpreter fi, int lineNumber, int maxGenerations, float mutationRate) {
        this.fi = fi;
        this.population = new int[lineNumber][fi.getColumnNumber()];
        this.generationsCount = 0;
        this.maxGenerations = maxGenerations;
        this.mutationRate = Math.max(0, Math.min(1, mutationRate));
        this.mutationLines = new LinkedList<>();
        this.convergenceCount = 0;
        this.hc = null;
        this.status = Status.IN_PROGRESS;
    }

    public void generateRandom(int[] firstLine) {
        LinkedList<Integer> possibleValues = new LinkedList<>();
        int startPoint = 0;
        if (firstLine != null) {
            startPoint = 1;
            this.population[0] = firstLine;
        }
        for (int i = 0; i < this.population[0].length; i++) {
            possibleValues.add(i + 1);
        }
        Random random = new Random();
        for (int i = startPoint; i < this.population.length; i++) {
            LinkedList<Integer> possibleValuesCopy = new LinkedList<>(possibleValues);
            for (int j = 0; j < this.population[0].length; j++) {
                int randomPosition = random.nextInt(possibleValuesCopy.size());
                int randomValue = possibleValuesCopy.remove(randomPosition);
                this.population[i][j] = randomValue;
            }
        }
        this.hc = new HeuristicCalculator(this.population, this.fi);
        if (this.hc.hasPerfectSolution()) {
            this.status = Status.PERFECT_SOLUTION_FOUND;
        }
        this.generationsCount = 0;
    }

    public void generateNext() {
        if (this.status != Status.IN_PROGRESS) {
            return;
        }
        Random random = new Random();
        int[][] nextPopulation = new int[this.population.length][this.population[0].length];
        nextPopulation[0] = this.hc.createElitismLine(); // <- Linha elitismo.
        for (int i = 1; i < nextPopulation.length - 1; i = i + 2) {
            // Torneios.
            int fatherPosition = fatherTournament();
            int[] father = this.population[fatherPosition];
            int motherPosition = motherTournament(fatherPosition);
            int[] mother = this.population[motherPosition];
            // Crossover.
            int[][] childs = crossover(father, mother);
            nextPopulation[i] = childs[0];
            nextPopulation[i + 1] = childs[1];
        }
        // Mutacao.
        this.mutationLines.clear();
        int mutationsCount = (int) Math.floor(this.mutationRate * (nextPopulation.length - 1));
        while (this.mutationLines.size() < mutationsCount) {
            int randomLine = random.nextInt(nextPopulation.length - 1) + 1;
            if (randomLine % 2 == 0) {
                if (!this.mutationLines.contains(randomLine) && !this.mutationLines.contains(randomLine - 1)) {
                    this.mutationLines.add(randomLine);
                }
            } else {
                if (!this.mutationLines.contains(randomLine) && !this.mutationLines.contains(randomLine + 1)) {
                    this.mutationLines.add(randomLine);
                }
            }
        }
        for (int i = 0; i < this.mutationLines.size(); i++) {
            int[] chromosome = nextPopulation[this.mutationLines.get(i)];
            mutate(chromosome);
        }
        // Configurando atributos da proxima geracao.
        this.generationsCount++;
        this.population = nextPopulation;
        this.hc = new HeuristicCalculator(nextPopulation, this.fi);
        // Checando convergencia.
        if (isInConvergence()) {
            convergenceCount++;
        } else {
            convergenceCount = 0;
        }
        float covergenceValue = (float) this.hc.bestValue() / this.hc.perfectSolutionValue() * 10;
        // Checando condicoes de parada.
        if (this.hc.hasPerfectSolution()) {
            this.status = Status.PERFECT_SOLUTION_FOUND;
        } else if (convergenceCount >= 15 - covergenceValue) {
            this.status = Status.CONVERGENCE_DETECTED;
        } else if (this.generationsCount >= this.maxGenerations) {
            this.status = Status.GENERATION_LIMIT_REACHED;
        }
    }

    private int fatherTournament() {
        Random random = new Random();
        int f1 = random.nextInt(this.population.length);
        int f2 = random.nextInt(this.population.length);
        while (f2 == f1) {
            f2 = random.nextInt(this.population.length);
        }
        return this.hc.getHeuristicAt(f1) >= this.hc.getHeuristicAt(f2) ? f1 : f2;
    }

    private int motherTournament(int fatherPosition) {
        Random random = new Random();
        int m1 = -1, m2 = -1;
        while (m1 == m2 || m1 == fatherPosition || m2 == fatherPosition) {
            m1 = random.nextInt(this.population.length);
            m2 = random.nextInt(this.population.length);
        }
        return this.hc.getHeuristicAt(m1) >= this.hc.getHeuristicAt(m2) ? m1 : m2;
    }

    // Classe auxiliar durante o crossover OBX.
    private class PriorityNode implements Comparable<PriorityNode> {

        private int position;
        private int value;
        private int priority;

        public PriorityNode(int position, int value, int priority) {
            this.position = position;
            this.value = value;
            this.priority = priority;
        }

        @Override
        public int compareTo(PriorityNode other) {
            if (this.priority > other.priority) {
                return -1;
            }
            if (this.priority < other.priority) {
                return 1;
            }
            return 0;
        }

        public int getPosition() {
            return this.position;
        }

        public int getValue() {
            return this.value;
        }

    }

    // Crossover OBX.
    private int[][] crossover(int[] father, int[] mother) {
        Random random = new Random();
        LinkedList<Integer> chosenPositions = new LinkedList<>();
        int positionsCount = father.length / 2;
        for (int i = 0; i < positionsCount; i++) {
            int randomPosition = random.nextInt(father.length);
            while (chosenPositions.contains(randomPosition)) {
                randomPosition = random.nextInt(father.length);
            }
            chosenPositions.add(randomPosition);
        }
        int[] child1 = new int[father.length];
        int[] child2 = new int[mother.length];
        LinkedList<PriorityNode> fatherPriorityList = new LinkedList<>();
        LinkedList<PriorityNode> motherPriorityList = new LinkedList<>();
        for (int i = 0; i < father.length; i++) {
            if (!chosenPositions.contains(i)) {
                child1[i] = father[i];
                child2[i] = mother[i];
            } else {
                int elementInFather = father[i];
                int elementInMother = mother[i];
                int fatherOccurenceInMother = getOccurenceOf(elementInFather, mother);
                int motherOccurenceInFather = getOccurenceOf(elementInMother, father);
                // position, value, priority
                fatherPriorityList.add(new PriorityNode(i, elementInFather, fatherOccurenceInMother * -1));
                motherPriorityList.add(new PriorityNode(i, elementInMother, motherOccurenceInFather * -1));
            }
        }
        // Ordenando com base nas prioridades.
        Collections.sort(fatherPriorityList);
        Collections.sort(motherPriorityList);
        for (int i = 0; i < fatherPriorityList.size(); i++) {
            PriorityNode fatherNode = fatherPriorityList.get(i);
            child1[fatherNode.getPosition()] = fatherNode.getValue();
            PriorityNode motherNode = motherPriorityList.get(i);
            child2[motherNode.getPosition()] = motherNode.getValue();
        }
        return new int[][] { child1, child2 };
    }

    // Metodo auxiliar durante o crossover OBX.
    private int getOccurenceOf(int element, int[] chromosome) {
        for (int i = 0; i < chromosome.length; i++) {
            if (chromosome[i] == element) {
                return i;
            }
        }
        return -1;
    }

    private void mutate(int[] chromosome) {
        float bestLineScore = (float) this.hc.bestValue() / this.hc.perfectSolutionValue();
        int minimumSwapCount = (int) Math.ceil((this.population[0].length * 0.05));
        // Percentage = 1. Troca sempre 5% das posicoes do cromossomo.
        mutateWithPercentage(chromosome, minimumSwapCount, 1);
        // Percentage = bestLineScore. Troca de 0% a 20% a mais das posicoes do
        // cromossomo.
        mutateWithPercentage(chromosome, minimumSwapCount * 4, 1 - bestLineScore);
        // Explicacao:
        // Em populacoes com valores muito bons de heuristica, a mutacao tende a trocar
        // mais perto de 5% das posicoes dos cromossomos, enquanto que em populacoes
        // com valores muito ruins de heuristica, a mutacao tende a trocar mais perto de
        // 25% das posicoes dos cromossomos.
    }

    private void mutateWithPercentage(int[] chromosome, int swapCount, float percentage) {
        Random random = new Random();
        for (int i = 0; i < swapCount; i++) {
            if (percentage > Math.random()) {
                int i1 = random.nextInt(chromosome.length);
                int i2 = random.nextInt(chromosome.length);
                while (i2 == i1) {
                    i2 = random.nextInt(chromosome.length);
                }
                int aux = chromosome[i1];
                chromosome[i1] = chromosome[i2];
                chromosome[i2] = aux;
            }
        }
    }

    private boolean isInConvergence() {
        int bestValue = this.hc.bestValue();
        int count = 0;
        for (int i = 0; i < this.population.length; i++) {
            if (this.hc.getHeuristicAt(i) == bestValue) {
                count++;
            }
        }
        // Sera considerado convergencia se 95% das linhas que nao sofreram mutacao
        // forem iguais.
        double linesToConverge = (this.population.length * (1 - this.mutationRate)) * 0.95;
        if (count >= linesToConverge) {
            return true;
        }
        return false;
    }

    public int[] bestLine() {
        return this.hc.createElitismLine();
    }

    public Status getStatus() {
        return this.status;
    }

    // Os metodos abaixo servem somente para printar
    // a populacao e nao fazem parte do algoritmo.

    public void printBestSolutionFound() {
        IO.writeln("Melhor resultado encontrado ([manha, tarde]):\n", ConsoleColors.WHITE_BOLD);
        for (int i = 0; i < this.population[0].length; i++) {
            IO.write("[" + this.population[hc.getBestLineIndex()][i] + ", " + (i + 1) + "] ");
        }
        IO.write("\n");
    }

    public void printPopulation() {
        if (this.generationsCount == 0) {
            IO.writeln("\nPOPULACAO INICIAL:\n", ConsoleColors.WHITE_BOLD);
        } else {
            IO.writeln("\nGERACAO " + this.generationsCount + ":\n", ConsoleColors.WHITE_BOLD);
        }
        int places = Integer.toString(this.population[0].length).length();
        int heuristicPlaces = Integer.toString(this.hc.perfectSolutionValue()).length();
        int bestLineIndex = this.hc.getBestLineIndex();
        for (int i = 0; i < places + 3; i++) {
            IO.write(" ");
        }
        for (int i = 0; i < this.population[0].length; i++) {
            IO.write("[" + formatLineNumber((i + 1), places) + "] ");
        }
        IO.write("\n");
        for (int i = 0; i < this.population.length; i++) {
            IO.write("[" + formatLineNumber(i, places) + "] ", ConsoleColors.CYAN);
            ConsoleColors lineColor = ConsoleColors.WHITE;
            if (this.mutationLines.contains(i)) {
                lineColor = ConsoleColors.MAGENTA;
            }
            ConsoleColors heuristicColor = getColorAt(i);
            for (int j = 0; j < this.population[0].length; j++) {
                IO.write(" " + formatLineNumber(this.population[i][j], places) + "  ", lineColor);
            }
            int lineHeuristic = this.hc.getHeuristicAt(i);
            IO.write("[" + formatLineNumber(lineHeuristic, heuristicPlaces) + "]", heuristicColor);
            if (i == bestLineIndex) {
                float score = (float) lineHeuristic / this.hc.perfectSolutionValue() * 100;
                IO.write(" <- Melhor Linha (" + lineHeuristic + " / " + this.hc.perfectSolutionValue()
                        + " = " + score + "%)", ConsoleColors.WHITE_BOLD);
            }
            IO.write("\n");
        }
    }

    private ConsoleColors getColorAt(int index) {
        int heuristic = this.hc.getHeuristicAt(index);
        int perfectSolutionValue = this.hc.perfectSolutionValue();
        float score = (float) heuristic / perfectSolutionValue;
        if (score < 0.6) {
            return ConsoleColors.RED;
        } else if (score < 0.8) {
            return ConsoleColors.YELLOW;
        }
        return ConsoleColors.GREEN;
    }

    private String formatLineNumber(int line, int places) {
        String formatedLine = Integer.toString(line);
        for (int i = formatedLine.length(); i < places; i++) {
            formatedLine = "0" + formatedLine;
        }
        return formatedLine;
    }

}
