package akkount.view.currency;

import akkount.entity.Currency;

import akkount.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "currencies/:id", layout = MainView.class)
@ViewController("akk_Currency.detail")
@ViewDescriptor("currency-detail-view.xml")
@EditedEntityContainer("currencyDc")
public class CurrencyDetailView extends StandardDetailView<Currency> {
}