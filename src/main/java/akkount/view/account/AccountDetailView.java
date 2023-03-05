package akkount.view.account;

import akkount.entity.Account;

import akkount.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "accounts/:id", layout = MainView.class)
@ViewController("akk_Account.detail")
@ViewDescriptor("account-detail-view.xml")
@EditedEntityContainer("accountDc")
public class AccountDetailView extends StandardDetailView<Account> {
}