import java.util.Scanner;

public class App {

    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        IO.writeln("\n[T1 - Inteligencia Artificial]\n", ConsoleColors.WHITE_BOLD);
        IO.write("OBS: ", ConsoleColors.YELLOW);
        IO.writeln(
                "Execute esse programa utilizando a ferramenta Visual Studio Code para que os arquivos possam ser devidamente encontrados.\n");

        // Solicitando nome do arquivo ao usuario.
        IO.write("Informe o nome do arquivo a ser utilizado (dentro da pasta 'assets'): ");
        String fileName = scanner.nextLine();
        fileName = fileName.contains(".txt") ? fileName : fileName + ".txt";

        // Lendo o conteudo do arquivo.
        String fileContent = IO.readFile("src/assets/" + fileName);

        if (fileContent == null) {
            return;
        }

        // Interpretando o conteudo do arquivo.
        FileInterpreter fileInterpreter = new FileInterpreter(fileContent);

        // Variaveis de configuracao do algoritmo.
        int lineNumber = 0, maxGenerations = 0, delay = 0;
        float mutationRate = 0.0f;
        int[] previousBestLine = null;

        String option = "1";

        while (true) {
            // Solicitando configuracoes do algoritmo ao usuario.
            if (option.equals("1")) {
                IO.write(
                        "Informe quantas linhas terao as matrizes das populacoes sem considerar a do elitismo (numero par): ");

                lineNumber = Integer.parseInt(scanner.nextLine());
                while (lineNumber <= 0 || lineNumber % 2 != 0) {
                    IO.write("Entrada invalida, digite um novo valor: ");
                    lineNumber = Integer.parseInt(scanner.nextLine());
                }
                lineNumber++;

                IO.write("Informe o numero maximo de geracoes: ");
                maxGenerations = Integer.parseInt(scanner.nextLine());

                IO.write("Informe a taxa de mutacao (ex: 0.1 = 10%): ");
                mutationRate = Float.parseFloat(scanner.nextLine());

                IO.write("Informe o intervalo (delay) em milissegundos entre a geracao de novas populacoes: ");
                delay = Integer.parseInt(scanner.nextLine());

                IO.write("\nCiano", ConsoleColors.CYAN_BOLD);
                IO.write(" - Numero da linha.");
                IO.write("\nMagenta", ConsoleColors.MAGENTA_BOLD);
                IO.write(" - Linhas que sofreram mutacao.");
                IO.write("\nVermelho", ConsoleColors.RED_BOLD);
                IO.write(" - Valor da heuristica < 60% em relacao a solucao perfeita.");
                IO.write("\nAmarelo", ConsoleColors.YELLOW_BOLD);
                IO.write(" - Valor da heuristica < 80% em relacao a solucao perfeita.");
                IO.write("\nVerde", ConsoleColors.GREEN_BOLD);
                IO.write(" - Valor da heuristica > 80% em relacao a solucao perfeita.\n\n");
                IO.writeln("Pressione enter para continuar...");
                scanner.nextLine();
            }

            // Gerando uma populacao aleatoria inicial.
            PopulationGenerator populationGenerator = new PopulationGenerator(fileInterpreter, lineNumber,
                    maxGenerations,
                    mutationRate);
            populationGenerator.generateRandom(previousBestLine);

            // Geracoes:
            while (populationGenerator.getStatus() == PopulationGenerator.Status.IN_PROGRESS) {
                // Exibindo na tela geracao por geracao.
                populationGenerator.printPopulation();
                populationGenerator.generateNext();
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }

            // Exibindo na tela a ultima geracao.
            populationGenerator.printPopulation();

            IO.writeln("\n<- RESULTADOS ->\n", ConsoleColors.WHITE_BOLD);

            // Validando se a solucao foi encontrada.
            switch (populationGenerator.getStatus()) {
                case GENERATION_LIMIT_REACHED: {
                    IO.write("Motivo de parada:\n", ConsoleColors.WHITE_BOLD);
                    IO.writeln("\nLimite de geracoes atingido.\n");
                }
                    break;
                case CONVERGENCE_DETECTED: {
                    IO.write("Motivo de parada:\n", ConsoleColors.WHITE_BOLD);
                    IO.writeln(
                            "\nConvergencia detectada, execute o algoritmo novamente para possiveis melhores resultados.\n");
                }
                    break;
                case PERFECT_SOLUTION_FOUND: {
                    IO.write("Motivo de parada:\n", ConsoleColors.WHITE_BOLD);
                    IO.writeln("\nA solucao perfeita foi encontrada!\n");
                }
                    break;
                default: {
                    break;
                }
            }
            populationGenerator.printBestSolutionFound();

            IO.writeln("\n<- RESULTADOS ->", ConsoleColors.WHITE_BOLD);

            IO.write(
                    "\nDeseja executar o algoritmo novamente, melhorado? (1 = Sim / 2 = Repetir com os mesmos parametros / Outros = Nao): ");
            option = scanner.nextLine();
            if (!option.equals("1") && !option.equals("2")) {
                break;
            }

            // Na proxima execucao do algoritmo, a linha 0 da matrix da populacao inicial
            // sera a igual a linha do elitismo da ultima populacao da execucao anterior.
            previousBestLine = populationGenerator.bestLine();
        }

    }
}
