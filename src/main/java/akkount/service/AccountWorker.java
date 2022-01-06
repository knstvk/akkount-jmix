package akkount.service;

import akkount.entity.Account;
import akkount.entity.Currency;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("akk_AccountWorker")
public class AccountWorker {

    @Inject
    private DataManager dataManager;

    @EventListener
    public void onAccountPersisting(EntitySavingEvent<Account> event) {
        Account account = event.getEntity();
        Currency currency = dataManager.load(Id.of(account.getCurrency())).one();
        account.setCurrencyCode(currency.getCode());
    }

}
