package akkount.screen.preferences;

import com.google.common.base.Strings;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@UiController("akk_PreferencesScreen")
@UiDescriptor("preferences-screen.xml")
public class PreferencesScreen extends Screen {

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private UserManager userManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe("changePasswordBtn")
    public void onChangePasswordBtnClick(Button.ClickEvent event) {
        Dialogs.InputDialogBuilder builder = dialogs.createInputDialog(this);

        builder.withCaption("Change password");

        PasswordField currentPasswField = uiComponents.create(PasswordField.class);
        currentPasswField.setCaption("Current password");
        currentPasswField.setWidthFull();

        PasswordField passwField = uiComponents.create(PasswordField.class);
        passwField.setCaption("New password");
        passwField.setWidthFull();

        PasswordField confirmPasswField = uiComponents.create(PasswordField.class);
        confirmPasswField.setCaption("Confirm password");
        confirmPasswField.setWidthFull();

        builder.withParameters(
                new InputParameter("currentPassword").withField(() -> currentPasswField),
                new InputParameter("password").withField(() -> passwField),
                new InputParameter("confirmPassword").withField(() -> confirmPasswField));

        builder.withValidator(validationContext -> getValidationErrors(passwField, confirmPasswField, validationContext));
        InputDialog inputDialog = builder.build();
        builder.withActions(DialogActions.OK_CANCEL, result -> okButtonAction(result));
        inputDialog.show();

    }

    private ValidationErrors getValidationErrors(PasswordField passwField, PasswordField confirmPasswField, InputDialog.ValidationContext validationContext) {
        String password = validationContext.getValue("password");
        String confirmPassword = validationContext.getValue("confirmPassword");
        ValidationErrors errors = new ValidationErrors();
        if (!Objects.equals(password, confirmPassword)) {
            errors.add(confirmPasswField, "Passwords do not match");
        }
        if (Strings.isNullOrEmpty(password)) {
            errors.add(passwField, "Password required");
        }
        if (errors.isEmpty()) {
            return ValidationErrors.none();
        }
        return errors;
    }

    private void okButtonAction(InputDialog.InputDialogResult result) {
        String userName = currentAuthentication.getUser().getUsername();

        if (result.getCloseActionType() == InputDialog.InputDialogResult.ActionType.OK) {
            String newPassword = result.getValue("password");
            String oldPassword = result.getValue("currentPassword");
            try {
                userManager.changePassword(userName, oldPassword, newPassword);
            } catch (PasswordNotMatchException e) {
                notifications.create()
                        .withType(Notifications.NotificationType.ERROR)
                        .withCaption("Wrong current password")
                        .show();
                return;
            }
            notifications.create()
                    .withType(Notifications.NotificationType.HUMANIZED)
                    .withCaption("Password changed")
                    .show();
        }
    }
}