package akkount.view.currency;

import akkount.entity.Currency;

import akkount.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "currencies", layout = MainView.class)
@ViewController("akk_Currency.list")
@ViewDescriptor("currency-list-view.xml")
@LookupComponent("currenciesTable")
@DialogMode(width = "50em", height = "37.5em")
public class CurrencyListView extends StandardListView<Currency> {
}