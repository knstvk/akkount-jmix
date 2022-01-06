package akkount.screen;

import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.formatter.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component("akk_DecimalFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DecimalFormatter implements Formatter<BigDecimal> {

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private FormatStringsRegistry formatStringsRegistry;

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
