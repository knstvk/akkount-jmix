package akkount.screen.account;

import akkount.entity.Account;
import io.jmix.ui.screen.*;

@UiController("akk_Account.edit")
@UiDescriptor("account-edit.xml")
@EditedEntityContainer("accountDc")
public class AccountEdit extends StandardEditor<Account> {
}