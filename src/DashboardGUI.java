import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class DashboardGUI {
    private String firstName;
    private String lastName;
    private String profileUrl;
    private String username;



    public DashboardGUI(String firstName, String lastName, String profileUrl, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileUrl=profileUrl;
        this.username = username;

    }

    public void start(Stage primaryStage, String profileUrl) {
        primaryStage.setTitle("TaskNotion");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);


        System.out.println(username);

        Calendar calendarApp = new Calendar(username, primaryStage);
        Scene calendarScene = calendarApp.createScene(profileUrl, firstName, lastName);

        root.getChildren().add(calendarScene.getRoot());

        Scene scene = new Scene(root, 1500, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        // asteapta 2 secunde si apoi verifica ziua de nastere + afisare popup
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> checkBirthday());
        delay.play();
    }

    private void checkBirthday() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("users.json")));
            JSONArray usersArray = new JSONArray(content);

            // cauta userul curent in lista de users
            JSONObject currentUser = getCurrentUser(usersArray);

            // verifica daca ziua curenta este ziua de nastere a userului
            if (currentUser != null) {
                LocalDate currentDate = LocalDate.now();
                LocalDate birthday = LocalDate.parse(currentUser.optString("birthday", ""));
                if (currentDate.getMonth() == birthday.getMonth() && currentDate.getDayOfMonth() == birthday.getDayOfMonth()) {
                    showBirthdayPopup();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject getCurrentUser(JSONArray usersArray) throws JSONException {
        for (int i = 0; i < usersArray.length(); i++) {
            Object userObject = usersArray.get(i);

            if (userObject instanceof JSONObject) {
                JSONObject user = (JSONObject) userObject;

                if (user.optString("firstName", "").equals(firstName) && user.optString("lastName", "").equals(lastName)) {
                    return user;
                }
            }
        }
        return null;
    }

    private void showBirthdayPopup() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("La mulți ani!");
            alert.setHeaderText("Ziua ta de naștere!");
            alert.setContentText("La mulți ani, " + firstName + " " + lastName + "!");

            FileInputStream fis = null;
            try {
                fis = new FileInputStream("src/assets/happyBirthday.png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Image image = new Image(fis);
            ImageView imageView = new ImageView(image);
            alert.getDialogPane().setContent(imageView);

            alert.showAndWait();

        });
    }
}
