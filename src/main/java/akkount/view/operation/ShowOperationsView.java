package akkount.view.operation;


import akkount.entity.Category;
import akkount.entity.CategoryType;
import akkount.entity.Operation;
import akkount.view.DecimalFormatter;
import akkount.view.main.MainView;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "ShowOperationsView", layout = MainView.class)
@ViewController("ShowOperationsView")
@ViewDescriptor("show-operations-view.xml")
public class ShowOperationsView extends StandardView {

    public static class Params {
        private final Category category;
        private final LocalDate fromDate;
        private final LocalDate toDate;
        private final String currencyCode;

        public Params(Category category, LocalDate fromDate, LocalDate toDate, String currencyCode) {
            this.category = category;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.currencyCode = currencyCode;
        }
    }

    private Params params;

    public void setParams(Params params) {
        this.params = params;
    }

    @Autowired
    private DatatypeRegistry datatypeRegistry;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private Messages messages;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DecimalFormatter decimalFormatter;

    @ViewComponent
    private H5 descriptionLabel;
    @ViewComponent
    private CollectionLoader<Operation> operationsDl;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean isExpense = params.category.getCatType().equals(CategoryType.EXPENSE);
        Datatype<LocalDate> dateDatatype = datatypeRegistry.get(LocalDate.class);

        descriptionLabel.setText(messageBundle.formatMessage("showOperationsDescription",
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

    @Supply(to = "operationsDataGrid.amount1", subject = "renderer")
    private Renderer<Operation> operationsDataGridAmount1Renderer() {
        return new TextRenderer<>(operation ->
                decimalFormatter.apply(operation.getAmount1()));
    }

    @Supply(to = "operationsDataGrid.acc2", subject = "renderer")
    private Renderer<Operation> operationsDataGridAcc2Renderer() {
        return new TextRenderer<>(operation ->
                decimalFormatter.apply(operation.getAmount2()));
    }
}