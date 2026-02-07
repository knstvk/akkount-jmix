package akkount.view.currency;

import akkount.entity.Currency;
import akkount.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Route(value = "currencies", layout = MainView.class)
@ViewController("akk_Currency.list")
@ViewDescriptor("currency-list-view.xml")
@LookupComponent("currenciesTable")
@DialogMode(width = "50em", height = "37.5em")
public class CurrencyListView extends StandardListView<Currency> {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private DataGrid<Currency> currenciesTable;
    @ViewComponent
    private CollectionLoader<Currency> currenciesDl;
    @ViewComponent
    private Button setBaseBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        updateSetBaseEnabled();
        currenciesTable.addSelectionListener(selection -> updateSetBaseEnabled());
    }

    @Subscribe("setBaseBtn")
    public void onSetBaseBtnClick(ClickEvent<Button> event) {
        Currency baseCurrency = currenciesTable.getSingleSelectedItem();
        if (baseCurrency == null) {
            return;
        }

        List<Currency> currencies = dataManager.load(Currency.class).all().list();
        for (Currency currency : currencies) {
            currency.setBase(currency.equals(baseCurrency));
        }
        dataManager.save(currencies.toArray());
        currenciesDl.load();
        updateSetBaseEnabled();
    }

    private void updateSetBaseEnabled() {
        Set<Currency> selected = currenciesTable.getSelectedItems();
        if (selected.size() != 1) {
            setBaseBtn.setEnabled(false);
            return;
        }
        Currency currency = selected.iterator().next();
        setBaseBtn.setEnabled(currency.getBase() == null || !currency.getBase());
    }
}
