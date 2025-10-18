package com.erp.view;

import com.erp.controller.ProdutoController;
import com.erp.model.Produto;
import com.erp.monitor.MonitorPerformace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.List;

public class ProdutoView {
    private final ProdutoController controller = new ProdutoController();
    private final ObservableList<Produto> produtos;

    public ProdutoView() {
        List<Produto> lista = controller.carregarProdutos();
        produtos = FXCollections.observableArrayList(lista);
    }

    public void start(Stage stage) {
        TableView<Produto> table = new TableView<>(produtos);
        table.setEditable(true);
        table.setMaxWidth(720);
        table.setPlaceholder(new Label("Nenhum produto cadastrado"));

        TableColumn<Produto, Integer> colId = new TableColumn<>("ID");
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        TableColumn<Produto, Double> colPreco = new TableColumn<>("Preço");

        TableId(colId);
        TableNome(colNome);
        TablePreco(colPreco);

        table.getColumns().addAll(colId, colNome, colPreco);

        TableStyles(table);

        Label labelNome = new Label("Adicionar nome: ");
        Label labelQtd = new Label("Adicionar quantidade: ");
        Label labelPreco = new Label("Adicionar preço: ");

        labelNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        labelQtd.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        labelPreco.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField textoNome = new TextField();
        TextField textoQtd = new TextField();
        TextField textoPreco = new TextField();

        CampoStyles(textoNome);
        CampoStyles(textoPreco);
        CampoStyles(textoQtd);

        // Label para mostrar quantidade
        Label lblInfo = new Label("Produtos: " + produtos.size());
        lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Botões principais

        Button btnDel = new Button("Excluir");
        btnDel.setOnAction(e -> {
            Produto selecionado = table.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                produtos.remove(selecionado);
                controller.salvarProdutos(produtos);
                lblInfo.setText("Produtos: " + produtos.size());
            }
        });

        // Botões de teste
        Button btnAdicionar = new Button("Teste: Adicionar produtos");
        btnAdicionar.setStyle("-fx-background-color: #004CA3; -fx-text-fill: white;");
        btnAdicionar.setOnAction(e -> {
            controller.adicionarProdutosEmMassa(produtos, textoNome.getText(), Integer.parseInt(textoQtd.getText()), Double.parseDouble(textoPreco.getText()));
            table.refresh();
            lblInfo.setText("Produtos: " + produtos.size());
        });

        btnAdicionar.setPadding(new Insets(5, 10, 10, 5));

        Button btnMemoria = new Button("Ver Memória");
        btnMemoria.setStyle("-fx-background-color: #5CA8FF; -fx-text-fill: white;");
        btnMemoria.setOnAction(e -> MonitorPerformace.exibirMemoria());

        Button btnLimpar = new Button("Limpar Tudo");
        btnLimpar.setStyle("-fx-background-color: #5CA8FF; -fx-text-fill: white;");
        btnLimpar.setOnAction(e -> {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Deseja realmente excluir TODOS os produtos?",
                    ButtonType.YES, ButtonType.NO);
            confirmacao.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    produtos.clear();
                    controller.salvarProdutos(produtos);
                    lblInfo.setText("Produtos: " + produtos.size());
                }
            });
        });

        VBox campoNome = new VBox(5, labelNome, textoNome);
        VBox campoQtd = new VBox(5, labelQtd, textoQtd);
        VBox campoPreco = new VBox(5, labelPreco, textoPreco);

        // Organização dos botões
        HBox botoesBasicos = new HBox(10, btnDel, lblInfo);
        botoesBasicos.setPadding(new Insets(10));

        Separator separador = new Separator();
        separador.setPadding(new Insets(5, 0, 5, 0));

        HBox botoesTeste = new HBox(10, btnMemoria, btnLimpar);
        botoesTeste.setPadding(new Insets(5, 10, 10, 10));

        HBox areaBotoes = new HBox(botoesTeste, btnAdicionar, botoesBasicos);

        HBox areaEntrada = new HBox(10, campoNome, campoQtd, campoPreco);
        VBox menu = new VBox(10, areaEntrada, separador, areaBotoes);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        root.setPadding(new Insets(10));
        root.setCenter(table);
        root.setBottom(menu);

        OcultarLinhas(table);

        Scene scene = new Scene(root, 700, 500);

        stage.setTitle("ERP - Sistema de Produtos (com Teste de Performance)");
        stage.setScene(scene);
        stage.show();

        // Exibe informações iniciais no console
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  SISTEMA INICIADO                      ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Produtos carregados: " + produtos.size());
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    public void TableId(TableColumn<Produto, Integer> id) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        id.setPrefWidth(180);
    }

    public void TableNome(TableColumn<Produto, String> nome) {
        nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nome.setCellFactory(TextFieldTableCell.forTableColumn());
        nome.setOnEditCommit(e -> {
            e.getRowValue().setNome(e.getNewValue());
            controller.salvarProdutos(produtos);
        });
        nome.setPrefWidth(250);
    }

    public void TablePreco(TableColumn<Produto, Double> preco) {
        preco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        preco.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        preco.setOnEditCommit(e -> {
            e.getRowValue().setPreco(e.getNewValue());
            controller.salvarProdutos(produtos);
        });
        preco.setPrefWidth(250);
    }

    public void TableStyles(TableView<Produto> table) {
        table.setStyle("-fx-background-color: white;");
    }

    public void CampoStyles(TextField campo) {
        campo.setStyle(
                "-fx-background-color: #B8D9FF;" +
                "-fx-text-fill: #002147; -fx-font-weight: bold;" +
                "-fx-max-width: 200;" +
                "-fx-max-height: 25;" +
                "-fx-padding: 5 10 10 5");
    }

    public void OcultarLinhas(TableView<Produto> table) {
        table.setFixedCellSize(25);
        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());
    }
}