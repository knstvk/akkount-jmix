package akkount.view.account;

import akkount.entity.Account;

import akkount.jmx.SampleDataGenerator;
import akkount.service.BalanceService;
import akkount.view.main.MainView;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

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
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private SecureOperations secureOperations;
    @Autowired
    private PolicyStore policyStore;
    @Autowired
    private MessageBundle messageBundle;

    @ViewComponent
    private DataGrid<Account> accountsTable;
    @ViewComponent("accountsTable.recalcBalance")
    private Action accountsTableRecalcBalance;

    @Subscribe
    public void onInit(InitEvent event) {
        accountsTableRecalcBalance.setVisible(secureOperations.isSpecificPermitted("get-balance", policyStore));
        accountsTable.setMultiSelect(true);
    }

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

    @Subscribe("accountsTable.recalcBalance")
    public void onAccountsTableRecalcBalance(ActionPerformedEvent event) {
        Set<Account> selected = accountsTable.getSelectedItems();
        if (!selected.isEmpty() && secureOperations.isSpecificPermitted("get-balance", policyStore)) {
            dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("recalcBalance.title"))
                    .withText(messageBundle.getMessage("recalcBalance.msg"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK) {
                                @Override
                                public void actionPerform(Component component) {
                                    for (Account account : selected) {
                                        balanceService.recalculateBalance(account.getId());
                                    }
                                }
                            },
                            new DialogAction(DialogAction.Type.CANCEL)
                    )
                    .open();
        }
    }
}