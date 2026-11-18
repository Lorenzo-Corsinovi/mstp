package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerArchivio {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(3000)) {
            System.out.println("Server avviato sulla porta 3000");

            while (true) {
                Socket client = server.accept();
                try {
                    gestisciClient(client);
                } catch (IOException e) {
                    System.out.println("Errore client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Errore server: " + e.getMessage());
        }
    }

    public static void gestisciClient(Socket client) throws IOException {
        try (client) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)
            );
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),
                true
            );

            String richiesta = in.readLine();
            if (richiesta == null) {
                return;
            }

            System.out.println("Richiesta ricevuta: " + richiesta);
            richiesta = richiesta.trim();

            if (richiesta.isEmpty()) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            String[] parti = richiesta.split("\\s+");
            String comando = parti[0];

            if (!comando.equals("LEGGI")) {
                rispostaErrore(out, "ERR UNKNOWNCMD");
                return;
            }

            if (parti.length != 2) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            String nomeFile = parti[1];
            if (nomeFile.contains("/") || nomeFile.contains("\\") || 
                nomeFile.contains("..") || !nomeFile.endsWith(".txt")) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            File cartella = new File("documenti");
            File file = new File(cartella, nomeFile);

            if (!file.isFile()) {
                rispostaErrore(out, "ERR NOTFOUND");
                return;
            }

            boolean statoInviato = false;
            try (BufferedReader lettoreFile = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                
                out.println("OK FILE");
                out.println();
                statoInviato = true;

                String riga;
                while ((riga = lettoreFile.readLine()) != null) {
                    out.println(riga);
                }

            } catch (IOException e) {
                if (!statoInviato) {
                    rispostaErrore(out, "ERR READ");
                } else {
                    System.out.println("Errore lettura: " + e.getMessage());
                }
            }
        }
    }

    public static void rispostaErrore(PrintWriter out, String errore) {
        out.println(errore);
        out.println();
    }
}