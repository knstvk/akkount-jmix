package akkount.web.currency;

import akkount.entity.Currency;
import io.jmix.ui.screen.*;

@UiController("akk_Currency.edit")
@UiDescriptor("currency-edit.xml")
@EditedEntityContainer("currencyDc")
public class CurrencyEdit extends StandardEditor<Currency> {
}