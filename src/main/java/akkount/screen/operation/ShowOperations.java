package akkount.screen.operation;

import akkount.entity.Category;
import akkount.entity.CategoryType;
import akkount.entity.Operation;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.Date;

@UiController("ShowOperations")
@UiDescriptor("show-operations.xml")
public class ShowOperations extends Screen {

    public static class Params {
        private Category category;
        private Date fromDate;
        private Date toDate;
        private String currencyCode;

        public Params(Category category, Date fromDate, Date toDate, String currencyCode) {
            this.category = category;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.currencyCode = currencyCode;
        }
    }

    @Inject
    private Label<String> descriptionLab;
    @Inject
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DatatypeRegistry datatypeRegistry;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private CollectionLoader<Operation> operationsDl;

    private Params params;

    public void setParams(Params params) {
        this.params = params;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean isExpense = params.category.getCatType().equals(CategoryType.EXPENSE);
        Datatype<Date> dateDatatype = datatypeRegistry.get(Date.class);
        descriptionLab.setValue(messageBundle.formatMessage("showOperationsDescription",
                isExpense ? messageBundle.getMessage("expenseTab") : messageBundle.getMessage("incomeTab"),
                params.category.getName(),
                dateDatatype.format(params.fromDate, currentAuthentication.getLocale()),
                dateDatatype.format(params.toDate, currentAuthentication.getLocale()),
                params.currencyCode
        ));

        operationsDl.setParameter("category", params.category);
        operationsDl.setParameter("fromDate", params.fromDate);
        operationsDl.setParameter("toDate", params.toDate);
        if (params.category.getCatType().equals(CategoryType.EXPENSE)) {
            operationsDl.setParameter("currency1", params.currencyCode);
        } else {
            operationsDl.setParameter("currency2", params.currencyCode);
        }
        operationsDl.load();
    }
}