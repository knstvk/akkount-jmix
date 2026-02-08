package akkount.view.operation;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.springframework.lang.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

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

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private DatatypeRegistry datatypeRegistry;
    @Autowired
    private UiComponents uiComponents;

    public void initAmount(TypedTextField<String> amountField, BigDecimal value) {
        amountField.getElement().executeJs("""
            let input = this.inputElement
            input.addEventListener("keypress", function(event) {
                if (event.which == 61) {
                    event.preventDefault()
                    var x = event.target.value
                    if (x.match(/([-+]?[0-9]*\\.?[0-9]+[\\-\\+\\*\\/])+([-+]?[0-9]*\\.?[0-9]+)/)) {
                        this.value = eval(x)
                    }
                }
            })
        """);

        JmixButton button = uiComponents.create(JmixButton.class);
        button.setIcon(VaadinIcon.CALC.create());
        button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        button.setTitle("Calculate (press '=')");
        button.addClickListener(clickEvent -> {
            ValidationErrors validationErrors = new ValidationErrors();
            BigDecimal amount = calculateAmount(amountField, validationErrors);
            if (amount != null) {
                String string = datatypeRegistry.get(BigDecimal.class).format(amount, currentAuthentication.getLocale());
                amountField.setTypedValue(string);
            }
        });
        amountField.setSuffixComponent(button);

        Datatype<BigDecimal> decimalDatatype = datatypeRegistry.get(BigDecimal.class);
        amountField.setValue(decimalDatatype.format(value, currentAuthentication.getLocale()));
    }

    @Nullable
    public BigDecimal calculateAmount(TypedTextField<String> amountField, ValidationErrors errors) {
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
