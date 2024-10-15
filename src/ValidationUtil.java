import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;

class ValidationUtil {
    static void addValidator(TextField field, Label errorLabel, String errorMessage) {
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (field.getText().isEmpty()) {
                    setErrorState(field, errorLabel, errorMessage);
                } else {
                    clearErrorState(field, errorLabel);
                }
            }
        });
    }

    private static void setErrorState(Control control, Label errorLabel, String errorMessage) {
        control.setStyle("-fx-border-color: red;");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(true);
        control.setUserData(false);
    }

    private static void clearErrorState(Control control, Label errorLabel) {
        control.setStyle("");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        control.setUserData(true);
    }


    static void addDateValidator(DatePicker datePicker, Label errorLabel, String errorMessage) {
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isAfter(LocalDate.now())) {
                setErrorState(datePicker, errorLabel, errorMessage);
            } else {
                clearErrorState(datePicker, errorLabel);
            }
        });
    }

    static void addPasswordValidator(PasswordField passwordField, Label errorLabel, String errorMessage) {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 6) {
                setErrorState(passwordField, errorLabel, errorMessage);
            } else {
                clearErrorState(passwordField, errorLabel);
            }
        });
    }

    static void addConfirmPasswordValidator(PasswordField confirmPasswordField, PasswordField passwordField,
                                            Label errorLabel, String errorMessage) {
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(passwordField.getText())) {
                setErrorState(confirmPasswordField, errorLabel, errorMessage);
            } else {
                clearErrorState(confirmPasswordField, errorLabel);
            }
        });
    }

    static boolean validateAll(Control... controls) {
        boolean isValid = true;
        for (Control control : controls) {
            if (control.getUserData() != null && !((boolean) control.getUserData())) {
                isValid = false;
            }
        }
        return isValid;
    }

}
