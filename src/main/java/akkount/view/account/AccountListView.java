package akkount.view.account;

import akkount.entity.Account;

import akkount.jmx.SampleDataGenerator;
import akkount.view.main.MainView;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "accounts", layout = MainView.class)
@ViewController("akk_Account.list")
@ViewDescriptor("account-list-view.xml")
@LookupComponent("accountsTable")
@DialogMode(width = "50em", height = "37.5em")
public class AccountListView extends StandardListView<Account> {

    @Autowired
    private SampleDataGenerator sampleDataGenerator;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;

    @Subscribe("generateSampleDataBtn")
    public void onGenerateSampleDataBtnClick(ClickEvent<Button> event) {
        dialogs.createInputDialog(this)
                .withHeader("Generate sample data")
                .withParameter(InputParameter.intParameter("days").withRequired(true).withLabel("Number of days"))
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        confirmAndGenerate(closeEvent.getValue("days"));
                    }
                })
                .open();
    }

    private void confirmAndGenerate(Integer days) {
        dialogs.createOptionDialog()
                .withHeader("Warning")
                .withText("All existing data will be erased. Are you sure you want to proceed?")
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(actionPerformedEvent -> {
                                    generate(days);
                                }),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    private void generate(Integer days) {
        sampleDataGenerator.removeAllData("ok");
        String result = sampleDataGenerator.generateSampleData(days);
        notifications.create(result).withCloseable(true).show();
    }
}