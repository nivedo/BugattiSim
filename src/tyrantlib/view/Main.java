package tyrantlib.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.dialog.Dialogs;
import tyrantlib.model.*;
import tyrantlib.controller.*;

import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private Stage primaryStage;
    private CardHandler handler;
    private ObservableList<Simulator> obsDeckList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception{
        if(!checkLicense()) {
            Dialogs.create().owner(primaryStage).title("Error!").masthead("License Invalid").message("Please Update Your CCS License!").showError();
            System.exit(1);
        }
        this.primaryStage = primaryStage;
        this.handler = CardHandler.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("BugattiSim.fxml"));

        Parent root = loader.load();
        BugattiController controller = loader.getController();

        primaryStage.setTitle("CrazyDiamond Composite Score " + BugattiController.VERSION);
        primaryStage.setScene(new Scene(root,1280,750));
        primaryStage.getIcons().add(new Image("file:icon.png"));

        controller.setMain(this);

        primaryStage.show();
    }

    public boolean checkLicense() {
        LicenseValidator lv = new LicenseValidator();
        int lvSeconds = lv.validate();

        int days = (int) TimeUnit.SECONDS.toDays(lvSeconds);
        if (lvSeconds <= 0) {
            return false;
        }

        return true;
    }

    public ObservableList<Simulator> getObsDeckList() {
        return obsDeckList;
    }

    public Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
