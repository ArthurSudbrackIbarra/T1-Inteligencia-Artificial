import java.util.HashMap;
import java.util.Map;

public class FileInterpreter {

    private int columnNumber;
    private Map<String, Integer> morningPreferences;
    private Map<String, Integer> eveningPreferences;

    public FileInterpreter(String fileContent) {
        setupPreferences(fileContent);
    }

    private void setupPreferences(String fileContent) {
        try {
            // Pegando as linhas do arquivo.
            String[] lines = fileContent.split("\n");
            // Na primeira linha temos o número de colunas.
            this.columnNumber = Integer.parseInt(lines[0]);
            // Alunos da manhã:
            this.morningPreferences = new HashMap<>();
            fillMap(1, columnNumber, lines, this.morningPreferences);
            // Alunos da tarde:
            this.eveningPreferences = new HashMap<>();
            fillMap(columnNumber + 1, columnNumber * 2, lines, this.eveningPreferences);
        } catch (Exception error) {
            IO.writeln(
                    "Erro de formatacao no arquivo. O arquivo nao esta nos padroes corretos e, portanto, nao foi possivel interpreta-lo.");
            System.exit(1);
        }
    }

    private void fillMap(int startPoint, int endPoint, String[] lines, Map<String, Integer> preferences) {
        for (int i = startPoint; i <= endPoint; i++) {
            String line = lines[i]; // "1 1 2 3"
            String[] lineDivision = line.split(" ", 2); // ["1", "1 2 3"]
            String currentStudent = lineDivision[0]; // "1"
            String[] otherStudents = lineDivision[1].split(" "); // ["1", "2", "3"]
            for (int j = 0; j < otherStudents.length; j++) {
                String otherStudent = otherStudents[j]; // "1", depois "2", depois "3"...
                int preference = Math.abs(j - otherStudents.length);
                preferences.put(currentStudent + "-" + otherStudent, preference);
                // No exemplo:
                // Chave: "1-1", Valor: 1
                // Chave "1-2", Valor: 2
                // Chave "1-3", Valor: 3
            }
        }
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    public int getMorningPreference(int currentStudent, int otherStudent) {
        String key = currentStudent + "-" + otherStudent;
        return this.morningPreferences.get(key);
    }

    public int getEveningPreference(int currentStudent, int otherStudent) {
        String key = currentStudent + "-" + otherStudent;
        return this.eveningPreferences.get(key);
    }

}