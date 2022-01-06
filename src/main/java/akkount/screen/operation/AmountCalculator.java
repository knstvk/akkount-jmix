package akkount.screen.operation;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AmountCalculator {

    private static final Pattern EXPR_PATTERN = Pattern.compile(
            "([-+]?[0-9]*\\.?[0-9]+[\\-\\+\\*/])+([-+]?[0-9]*\\.?[0-9]+)");

    @Autowired
    protected ScriptEvaluator scriptEvaluator;

    @Inject
    private CurrentAuthentication currentAuthentication;

    @Inject
    private DatatypeRegistry datatypeRegistry;

    public void initAmount(TextField amountField, BigDecimal value) {
        com.vaadin.ui.TextField vTextField = amountField.unwrap(com.vaadin.ui.TextField.class);
        new CalcExtension(vTextField);

        Datatype<BigDecimal> decimalDatatype = datatypeRegistry.get(BigDecimal.class);
        amountField.setValue(decimalDatatype.format(value, currentAuthentication.getLocale()));
    }

    @Nullable
    public BigDecimal calculateAmount(TextField<String> amountField, ValidationErrors errors) {
        String text = amountField.getValue();
        if (StringUtils.isBlank(text))
            return null;

        Matcher matcher = EXPR_PATTERN.matcher(text);
        if (matcher.matches()) {
            Number number = (Number) scriptEvaluator.evaluate(new StaticScriptSource(text));
            return BigDecimal.valueOf(number.doubleValue());
        } else {
            try {
                return datatypeRegistry.get(BigDecimal.class).parse(text, currentAuthentication.getLocale());
            } catch (ParseException e) {
                errors.add(amountField, e.getMessage());
                return null;
            }
        }
    }
}
