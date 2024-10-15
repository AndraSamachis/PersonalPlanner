import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LogInFormGUI {
    private static TextField usernameField;
    private static PasswordField passwordField;

    public static Scene createLogInScene(Stage primaryStage) {
        primaryStage.setTitle("Autentificare");
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);


        Label usernameLabel = new Label("Utilizator");

        Label passwordLabel = new Label("Parolă");

        passwordField = new PasswordField();
        usernameField = new TextField();
        usernameField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("text-field");

        Button registerButton = new Button("Înregistrează-te");
        registerButton.getStyleClass().add("buttonRegister");
        registerButton.setOnAction(event -> {
            RegisterFormGUI.start(new Stage());

            primaryStage.close();
        });


        Button loginButton = new Button("Autentifică-te");
        loginButton.getStyleClass().add("buttonLogin");
        Separator separator = new Separator();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        grid.add(usernameField,0,2);
        usernameField.setPromptText("Nume utilizator");
        passwordField.setPromptText("Parolă");
        grid.add(passwordField, 0, 4);
        grid.add(loginButton,0, 6 );

        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            JSONArray usersArray;
            try {
                String content = new String(Files.readAllBytes(Paths.get("users.json")));
                usersArray = new JSONArray(content);
                boolean authenticated = false;
                String firstName = "";
                String lastName = "";
                String profileUrl= "";

                for (int i = 0; i < usersArray.length(); i++) {
                    Object userObject = usersArray.get(i);

                    if (userObject instanceof JSONObject) {
                        JSONObject user = (JSONObject) userObject;

                        String storedPassword = null;
                        try {
                            storedPassword = user.getString("password");
                            String storedUsername = user.getString("username");

                            if (username.equals(storedUsername) && password.equals(storedPassword)) {
                                authenticated = true;
                                firstName = user.optString("firstName", "");
                                lastName = user.optString("lastName", "");
                                profileUrl = user.optString("profilePhotoUrl","");
                                break;
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                if (authenticated) {
                    System.out.println(username);

                    DashboardGUI dashboard = new DashboardGUI(firstName, lastName, profileUrl, username);
                    dashboard.start(primaryStage, profileUrl);
                } else {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Eroare");
                    alert.setHeaderText("Autentificare eșuată");
                    alert.setContentText("Verificați utilizatorul și parola.");
                    alert.showAndWait();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return;
            }
        });

        grid.add(separator, 0, 7);
        grid.add(registerButton, 0, 8);
        root.getChildren().addAll(grid);
        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().setStyle("-fx-background-color: #FFF0CE;");

        scene.getStylesheets().add("styles.css");
        return scene;
    }
}
