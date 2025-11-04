package br.com.le.addon.qualitymanagement.actionButtons;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LeArquivoTeste {
    public static void main(String[] args) {
        String filename = "C:\\Users\\teste.teste\\Documents\\sample.pdf";
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader in = new BufferedReader(fr);
            String line = in.readLine();
            while (line != null) {
                System.out.println(line);
                line = in.readLine();
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo \"" + filename + "\" nexiste.");
        } catch (IOException e) {
            System.out.println("Erro na leitura do arquivo " + filename + ".");
        }
    }
}
