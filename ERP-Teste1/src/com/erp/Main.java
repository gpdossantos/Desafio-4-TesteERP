package com.erp;

import com.erp.view.ProdutoView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ProdutoView view = new ProdutoView();
        view.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

