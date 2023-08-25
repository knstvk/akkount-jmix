package akkount.view.operation;


import akkount.entity.Category;
import akkount.entity.CategoryType;
import akkount.entity.Operation;
import akkount.view.DecimalFormatter;
import akkount.view.main.MainView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

@Route(value = "ShowOperationsView", layout = MainView.class)
@ViewController("ShowOperationsView")
@ViewDescriptor("show-operations-view.xml")
public class ShowOperationsView extends StandardView {

    public static class Params {
        private final Category category;
        private final Date fromDate;
        private final Date toDate;
        private final String currencyCode;

        public Params(Category category, Date fromDate, Date toDate, String currencyCode) {
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
    private DataGrid<Operation> operationsDataGrid;
    @ViewComponent
    private H5 descriptionLabel;
    @ViewComponent
    private CollectionLoader<Operation> operationsDl;


    @Subscribe
    public void onInit(InitEvent event) {
        Grid.Column<Operation> dateColumn = operationsDataGrid.addColumn(operation -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(messages.getMessage("opDateFormat"));
            return simpleDateFormat.format(operation.getOpDate());
        });
        dateColumn.setKey("opDate");
        dateColumn.setHeader(messages.getMessage("akkount.entity/Operation.opDate"));
        dateColumn.setResizable(true);
        dateColumn.setSortable(true);
        operationsDataGrid.setColumnPosition(dateColumn, 1);


        Grid.Column<Operation> amount1Column = operationsDataGrid.addColumn(operation ->
                decimalFormatter.apply(operation.getAmount1()));
        amount1Column.setKey("amount1");
        amount1Column.setHeader(messages.getMessage("akkount.entity/Operation.amount1"));
        amount1Column.setResizable(true);
        amount1Column.setSortable(true);
        operationsDataGrid.setColumnPosition(amount1Column, 3);

        Grid.Column<Operation> amount2Column = operationsDataGrid.addColumn(operation ->
                decimalFormatter.apply(operation.getAmount2()));
        amount2Column.setKey("amount2");
        amount2Column.setHeader(messages.getMessage("akkount.entity/Operation.amount2"));
        amount2Column.setResizable(true);
        amount2Column.setSortable(true);
        operationsDataGrid.setColumnPosition(amount2Column, 5);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean isExpense = params.category.getCatType().equals(CategoryType.EXPENSE);
        Datatype<Date> dateDatatype = datatypeRegistry.get(Date.class);

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
}