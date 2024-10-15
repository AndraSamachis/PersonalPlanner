import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("TaskNotion");


        Stage loginStage = new Stage();
        loginStage.setTitle("Autentificare");
        Scene loginScene = LogInFormGUI.createLogInScene(loginStage);
        loginStage.setScene(loginScene);
        loginStage.show();

    }

}
