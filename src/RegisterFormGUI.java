import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class RegisterFormGUI {
    private static TextField firstnameField;
    private static TextField lastnameField;
    private static TextField usernameField;
    private static DatePicker birthdayPicker;
    private static PasswordField passwordField;
    private static PasswordField confirmPasswordField;
    private static Circle photoCircle;
    private static Image selectedPhoto;

    public static Image getSelectedPhoto() {
        return selectedPhoto;
    }
    public static String profileUrl ;

    public static Scene createRegisterScene(Stage primaryStage) {
        primaryStage.setTitle("Înregistrare");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);
        photoCircle = new Circle(80);
        selectedPhoto = new Image("assets/profile-user.png");
        photoCircle.setFill(new ImagePattern(selectedPhoto));

        Label firstnameLabel = new Label("Nume");
        Label lastnameLabel = new Label("Prenume");
        Label usernameLabel = new Label("Nume de utilizator");
        Label emailLabel = new Label("E-mail");
        Label birthdayLabel = new Label("Data nașterii");
        Label passwordLabel = new Label("Parolă");
        Label confirmPasswordLabel = new Label("Confirmare parolă");

        Button backToLoginButton = new Button("Înapoi la autentificare");
        backToLoginButton.getStyleClass().add("buttonBackToLogin");

        Button addPhotoButton = new Button("Adaugă o fotografie");
        VBox photoBox = new VBox();

        photoBox.getChildren().addAll(photoCircle, addPhotoButton);
        GridPane.setMargin(photoBox, new Insets(0, 10, 0, 70));

        addPhotoButton.getStyleClass().add("addPhotoButton");
        addPhotoButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selectați o fotografie");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
            );

            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                profileUrl=selectedFile.toURI().toString();
                try {
                    selectedPhoto = new javafx.scene.image.Image(selectedFile.toURI().toString());
                } catch (IllegalArgumentException e) {
                    // Handle the exception (e.g., display an error message)
                    e.printStackTrace();
                    selectedPhoto = null; // Set selectedPhoto to null or provide a default image
                }
                photoCircle.setFill(new javafx.scene.paint.ImagePattern(selectedPhoto));
            }
        });

        backToLoginButton.setOnAction(event -> {
            Stage loginStage = new Stage();
            Scene loginScene = LogInFormGUI.createLogInScene(loginStage);
            loginStage.setScene(loginScene);
            loginStage.show();

            primaryStage.close();
        });



        firstnameField = new TextField();
        lastnameField = new TextField();
        usernameField = new TextField();
        passwordField = new PasswordField();
        birthdayPicker = new DatePicker();
        setMaxDateForDatePicker(birthdayPicker, LocalDate.now());
        confirmPasswordField = new PasswordField();
        firstnameField.getStyleClass().add("text-field");
        lastnameField.getStyleClass().add("text-field");
        usernameField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("text-field");
        birthdayPicker.getStyleClass().add("birthday-picker");
        confirmPasswordField.getStyleClass().add("text-field");
        Button registerButton = new Button("Înregistrează-te");

        registerButton.getStyleClass().add("buttonRegister");
        backToLoginButton.getStyleClass().add("buttonLogin");
        ImageView errorIcon = new ImageView(new Image("assets/cancel-2.png"));
        errorIcon.setFitWidth(15);
        errorIcon.setFitHeight(15);

        Label firstnameErrorLabel = new Label();
        Label lastnameErrorLabel = new Label();
        Label usernameErrorLabel = new Label();
        Label birthdayErrorLabel = new Label();
        Label passwordErrorLabel = new Label();
        Label confirmPasswordErrorLabel = new Label();

        ValidationUtil.addValidator(firstnameField, firstnameErrorLabel, "Numele este obligatoriu.");
        ValidationUtil.addValidator(lastnameField, lastnameErrorLabel, "Prenumele este obligatoriu.");
        ValidationUtil.addValidator(usernameField, usernameErrorLabel, "Numele de utilizator este obligatoriu.");
        ValidationUtil.addDateValidator(birthdayPicker, birthdayErrorLabel, "Data de nastere este invalida.");
        ValidationUtil.addPasswordValidator(passwordField, passwordErrorLabel, "Parola trebuie sa aiba cel putin 6 caractere.");
        ValidationUtil.addConfirmPasswordValidator(confirmPasswordField, passwordField, confirmPasswordErrorLabel, "Parolele nu coincid.");
        ValidationUtil.addDateValidator(birthdayPicker, birthdayErrorLabel, "Data de nastere este invalida.");

        grid.add(photoBox, 1, 1);
        grid.add(firstnameLabel, 0, 2);
        grid.add(firstnameField, 1, 2);
        grid.add(lastnameLabel, 0, 3);
        grid.add(lastnameField, 1, 3);
        grid.add(usernameLabel, 0, 4);
        grid.add(usernameField, 1, 4);
        grid.add(birthdayLabel, 0, 5);
        grid.add(birthdayPicker, 1, 5);
        grid.add(passwordLabel, 0, 6);
        grid.add(passwordField, 1, 6);
        grid.add(confirmPasswordLabel, 0, 7);
        grid.add(confirmPasswordField, 1, 7);
        grid.add(registerButton, 1, 8);

        root.getChildren().addAll(grid);
        grid.add(backToLoginButton, 1, 9);


        registerButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String firstName = firstnameField.getText();
            String lastName = lastnameField.getText();
            String birthday = birthdayPicker.getValue().toString();

            JSONObject userObject = new JSONObject();
            try {
                userObject.put("username", username);
                userObject.put("password", password);
                userObject.put("firstName", firstName);
                userObject.put("lastName", lastName);
                userObject.put("birthday", birthday);
                if(profileUrl!=null){
                    userObject.put("profilePhotoUrl", profileUrl);
                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


            JSONArray usersArray;
            try {
                usersArray = new JSONArray(FileUtils.readFileToString(new File("users.json"), "UTF-8"));
            } catch (IOException | JSONException e) {
                usersArray = new JSONArray();
            }

            usersArray.put(userObject);
            try (FileWriter userFile = new FileWriter("tasks_" + username + ".txt")) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileWriter file = new FileWriter("users.json")) {
                if (ValidationUtil.validateAll(
                        firstnameField, lastnameField, usernameField, birthdayPicker, passwordField, confirmPasswordField)) {
                    file.write(usersArray.toString());

                }

                Stage loginStage = new Stage();
                Scene loginScene = LogInFormGUI.createLogInScene(loginStage);
                loginStage.setScene(loginScene);
                loginStage.show();

                primaryStage.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().setStyle("-fx-background-color: #FFF0CE;");
        scene.getStylesheets().add("styles.css");
        return scene;
    }

    private static void setMaxDateForDatePicker(DatePicker datePicker, LocalDate maxDate) {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(maxDate));
            }
        });
    }

    private static void handle(ActionEvent event) throws JSONException {

        String username = usernameField.getText();
        String password = passwordField.getText();
        String firstName = firstnameField.getText();
        String lastName = lastnameField.getText();
        String birthday = birthdayPicker.getValue().toString();

        JSONObject userObject = new JSONObject();
        userObject.put("username", username);
        userObject.put("password", password);
        userObject.put("firstName", firstName);
        userObject.put("lastName", lastName);
        userObject.put("birthday", birthday);

        JSONArray usersArray;
        try {
            usersArray = new JSONArray(FileUtils.readFileToString(new File("users.json"), "UTF-8"));
        } catch (IOException e) {
            usersArray = new JSONArray();
        }

        usersArray.put(userObject);

        try (FileWriter file = new FileWriter("users.json")) {
            file.write(usersArray.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void start(Stage registerStage) {
        Scene registerScene = createRegisterScene(registerStage);
        registerStage.setScene(registerScene);
        registerStage.show();
    }
}


