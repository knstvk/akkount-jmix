package akkount.view.preferences;


import akkount.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.view.changepassword.ChangePasswordView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "preferences", layout = MainView.class)
@ViewController("akk_PreferencesView")
@ViewDescriptor("preferences-view.xml")
public class PreferencesView extends StandardView {

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe("changePasswordBtn")
    public void onChangePasswordBtnClick(ClickEvent<Button> event) {
        DialogWindow<ChangePasswordView> dialog = dialogWindows.view(this, ChangePasswordView.class)
                .build();
        ChangePasswordView view = dialog.getView();
        view.setUsername(currentAuthentication.getUser().getUsername());
        view.setCurrentPasswordRequired(true);
        dialog.open();
    }
}