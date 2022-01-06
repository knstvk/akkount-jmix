package akkount.web.account;

import akkount.entity.Account;
import akkount.service.BalanceService;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Component;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

@UiController("akk_Account.lookup")
@UiDescriptor("account-browse.xml")
@LookupComponent("accountTable")
public class AccountBrowse extends StandardLookup<Account> {

    @Inject
    private Table<Account> accountTable;

    @Inject
    private BalanceService balanceService;

    @Inject
    private Dialogs dialogs;

    @Inject
    private MessageBundle messageBundle;

    @Autowired
    private PolicyStore policyStore;

    @Autowired
    private SecureOperations secureOperations;
    @Named("accountTable.recalcBalance")
    private Action accountTableRecalcBalance;

    @Subscribe
    public void onInit(InitEvent event) {
        accountTableRecalcBalance.setVisible(secureOperations.isSpecificPermitted("get-balance", policyStore));
    }

    @Subscribe("accountTable.recalcBalance")
    public void onAccountTableRecalcBalance(Action.ActionPerformedEvent event) {
        Set<Account> selected = accountTable.getSelected();
        if (!selected.isEmpty() && secureOperations.isSpecificPermitted("get-balance", policyStore)) {
            dialogs.createOptionDialog()
                    .withCaption(messageBundle.getMessage("recalcBalance.title"))
                    .withMessage(messageBundle.getMessage("recalcBalance.msg"))
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
                    .show();
        }
    }
}