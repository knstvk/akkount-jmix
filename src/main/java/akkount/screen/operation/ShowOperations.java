package akkount.screen.operation;

import akkount.entity.Category;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;

@UiDescriptor
@UiController("show-operations.xml")
public class ShowOperations extends Screen {

    @Inject
    private Label<String> descriptionLab;
    @Inject
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DatatypeRegistry datatypeRegistry;

    @Autowired
    private MessageBundle messageBundle;

    @Subscribe
    public void onInit(InitEvent event) {
        Map<String, Object> params = ((MapScreenOptions) event.getOptions()).getParams();
        boolean isExpense = params.get("currency1") != null;
        Datatype<Date> dateDatatype = datatypeRegistry.get(Date.class);
        descriptionLab.setValue(messageBundle.formatMessage("showOperationsDescription",
                isExpense ? messageBundle.getMessage("expenseTab") : messageBundle.getMessage("incomeTab"),
                ((Category) params.get("category")).getName(),
                dateDatatype.format(params.get("fromDate"), currentAuthentication.getLocale()),
                dateDatatype.format(params.get("toDate"), currentAuthentication.getLocale()),
                isExpense ? params.get("currency1") : params.get("currency2")
        ));
    }
}