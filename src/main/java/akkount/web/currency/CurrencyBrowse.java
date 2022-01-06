package akkount.web.currency;

import akkount.entity.Currency;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("akk_Currency.lookup")
@UiDescriptor("currency-browse.xml")
@LookupComponent("currencyTable")
public class CurrencyBrowse extends StandardLookup<Currency> {
}