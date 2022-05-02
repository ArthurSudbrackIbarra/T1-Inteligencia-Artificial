import java.util.Arrays;

public class HeuristicCalculator {

    private int[][] population;
    private FileInterpreter fi;
    private int[] heuristics;
    private int bestValueIndex;
    private int perfectSolution;

    public HeuristicCalculator(int[][] population, FileInterpreter fileInterpreter) {
        this.population = population;
        this.fi = fileInterpreter;
        this.heuristics = new int[population.length];
        this.bestValueIndex = 0;
        this.perfectSolution = (int) Math.pow(population[0].length, 2) * 2;
        calculateHeuristics();
    }

    private void calculateHeuristics() {
        for (int i = 0; i < this.population.length; i++) {
            for (int j = 0; j < this.population[i].length; j++) {
                this.heuristics[i] += this.fi.getMorningPreference(j + 1, this.population[i][j]);
                this.heuristics[i] += this.fi.getEveningPreference(this.population[i][j], j + 1);
                if (this.heuristics[i] > this.heuristics[this.bestValueIndex]) {
                    this.bestValueIndex = i;
                }
            }
        }
    }

    public int getHeuristicAt(int index) {
        return this.heuristics[index];
    }

    public int getBestLineIndex() {
        return this.bestValueIndex;
    }

    public int bestValue() {
        return this.heuristics[this.bestValueIndex];
    }

    public int[] createElitismLine() {
        return Arrays.copyOf(this.population[this.bestValueIndex], this.population[this.bestValueIndex].length);
    }

    public int perfectSolutionValue() {
        return this.perfectSolution;
    }

    public boolean hasPerfectSolution() {
        return bestValue() == perfectSolutionValue();
    }

}
