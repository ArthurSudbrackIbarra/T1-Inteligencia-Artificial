import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IO {

    public static String readFile(String filePath) {
        File file = new File(filePath);
        BufferedReader bReader;
        FileReader fReader;
        try {
            fReader = new FileReader(file);
            bReader = new BufferedReader(fReader);
            String fileContent = "";
            String line;
            while ((line = bReader.readLine()) != null) {
                fileContent += line + "\n";
            }
            bReader.close();
            return fileContent;
        } catch (IOException error) {
            System.out.println("Erro ao tentar ler o arquivo: " + error);
            return null;
        }
    }

    public static void write(Object x) {
        System.out.print(x);
    }

    public static void writeln(Object x) {
        System.out.println(x);
    }

    public static void write(Object x, ConsoleColors colorOptions) {
        System.out.print(colorOptions.toString() + x + ConsoleColors.RESET);
    }

    public static void writeln(Object x, ConsoleColors colorOptions) {
        System.out.println(colorOptions.toString() + x + ConsoleColors.RESET);
    }

}
