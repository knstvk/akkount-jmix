package akkount.view.account;

import akkount.entity.Account;

import akkount.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "accounts", layout = MainView.class)
@ViewController("akk_Account.list")
@ViewDescriptor("account-list-view.xml")
@LookupComponent("accountsTable")
@DialogMode(width = "50em", height = "37.5em")
public class AccountListView extends StandardListView<Account> {
}