package com.erp.controller;

import com.erp.model.Produto;
import com.erp.monitor.MonitorPerformace;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProdutoController {
    private static final String FILE_PATH = "src/com/erp/recourse/produtos.json";
    private static final int MAX_PRODUTOS = 10000;
    private final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(ProdutoController.class.getName());

    // Flag para ativar/desativar monitoramento
    private boolean monitorarPerformance = true;

    public List<Produto> carregarProdutos() {
        if (monitorarPerformance) {
            var resultado = MonitorPerformace.medirCompleto(
                    "Carregar Produtos do JSON",
                    this::carregarProdutosInterno
            );
            return resultado.getResultado();
        }
        return carregarProdutosInterno();
    }

    private List<Produto> carregarProdutosInterno() {
        List<Produto> produtos = new ArrayList<>();
        File file = new File(FILE_PATH);

        try {
            if (!file.exists()) {
                LOGGER.info("Arquivo não existe. Criando novo arquivo: " + FILE_PATH);
                criarArquivoInicial(produtos);
            } else {
                produtos = lerArquivoJson(file);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar arquivo inicial", e);
            showError("Erro ao criar arquivo de produtos");
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Erro ao ler JSON - arquivo corrompido", e);
            showError("Arquivo de produtos corrompido. Criando backup...");
            backupAndReset(file, produtos);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao carregar produtos", e);
            showError("Erro ao carregar produtos");
        }

        return produtos;
    }

    private List<Produto> lerArquivoJson(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Produto>>(){}.getType();
            List<Produto> produtos = gson.fromJson(reader, listType);

            if (produtos == null) {
                LOGGER.warning("JSON retornou null, inicializando lista vazia");
                return new ArrayList<>();
            }

            // Filtra placeholders ao carregar
            produtos.removeIf(p -> p.getNome().equals("---"));
            return produtos;
        }
    }

    private void criarArquivoInicial(List<Produto> produtos) throws IOException {
        File file = new File(FILE_PATH);
        File parentDir = file.getParentFile();

        salvarProdutos(produtos);
    }

    private void backupAndReset(File file, List<Produto> produtos) {
        try {
            File backup = new File(FILE_PATH + ".backup");
            if (file.renameTo(backup)) {
                LOGGER.info("Backup criado: " + backup.getName());
            }
            criarArquivoInicial(produtos);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar backup", e);
        }
    }

    public boolean salvarProdutos(List<Produto> produtos) {
        if (monitorarPerformance) {
            var resultado = MonitorPerformace.medirTempo(
                    "Salvar " + produtos.size() + " Produtos",
                    () -> salvarProdutosInterno(produtos)
            );
            return resultado.getResultado();
        }
        return salvarProdutosInterno(produtos);
    }

    private boolean salvarProdutosInterno(List<Produto> produtos) {
        if (produtos == null) {
            LOGGER.warning("Tentativa de salvar lista null");
            return false;
        }

        // Filtra placeholders antes de salvar
        List<Produto> produtosReais = new ArrayList<>();
        for (Produto p : produtos) {
            if (!p.getNome().equals("---")) {
                produtosReais.add(p);
            }
        }

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(produtosReais, writer);
            writer.flush();
            LOGGER.info("Produtos salvos com sucesso");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar produtos", e);
            showError("Erro ao salvar produtos no arquivo");
            return false;
        }
    }

    public int getProximoId(List<Produto> produtos) {
        int maxId = 0;
        for (Produto p : produtos) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return maxId + 1;
    }

    public boolean atingiuLimite(List<Produto> produtos) {
        return produtos.size() >= MAX_PRODUTOS;
    }

    // Método para exibir estatísticas de memória
    public void exibirEstatisticas() {
        MonitorPerformace.exibirMemoria();
    }

    // Método para ativar/desativar monitoramento
    public void setMonitorarPerformance(boolean ativar) {
        this.monitorarPerformance = ativar;
    }

    /**
     * Adiciona múltiplos produtos de uma vez para teste de performance
     */
    public boolean adicionarProdutosEmMassa(List<Produto> produtos,
                                            String nome,
                                            int qtd,
                                            double preco ) {


        if (!atingiuLimite(produtos)) {
            System.out.println("\nINICIANDO TESTE DE CARGA - " + qtd + " PRODUTOS\n");

            var resultado = MonitorPerformace.medirCompleto(
                    "Adicionar " + qtd + " produtos em massa",
                    () -> {
                        int idInicial = getProximoId(produtos);
                        int idAtual;
                        List<Produto> novos = new ArrayList<>();

                        for (int i = 0; i < qtd; i++) {
                            idAtual = idInicial + i;
                            novos.add(new Produto(idAtual, nome, preco));
                        }

                        produtos.addAll(novos);
                        return salvarProdutosInterno(produtos);
                    }
            );

            System.out.println("✅ Total de produtos agora: " + produtos.size() + "\n");
            return resultado.getResultado();
        }
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Limite de 10000 produtos atingido!", ButtonType.OK);
        alert.showAndWait();
        return atingiuLimite(produtos);

    }

    /**
     * Teste completo de performance com comparação
     */


    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    message,
                    javafx.scene.control.ButtonType.OK
            );
            alert.setHeaderText("Erro no Sistema");
            alert.showAndWait();
        });
    }
}