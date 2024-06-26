package akkount.web;

import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.formatter.Formatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DecimalFormatter implements Formatter<BigDecimal> {

    private CurrentAuthentication currentAuthentication;
    private FormatStringsRegistry formatStringsRegistry;

    public DecimalFormatter(CurrentAuthentication currentAuthentication, FormatStringsRegistry formatStringsRegistry) {
        this.currentAuthentication = currentAuthentication;
        this.formatStringsRegistry = formatStringsRegistry;
    }

    @Override
    public String apply(BigDecimal value) {
        if (value == null)
            return null;
        if (BigDecimal.ZERO.compareTo(value) == 0)
            return "";
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
        DecimalFormat format = new DecimalFormat("#,###", formatStrings.getFormatSymbols());
        return format.format(value);
    }
}
