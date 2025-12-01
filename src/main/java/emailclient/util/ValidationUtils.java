package emailclient.util;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

public class ValidationUtils {

    public static void showError(Control field, Label label, String message) {
        field.getStyleClass().add("error-field");

        if (label != null)
            label.setText(message);
    }

    public static void clearError(Control field, Label label) {
        field.getStyleClass().remove("error-field");

        if (label != null)
            label.setText("");
    }

    public static boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}

