package akkount.web.operation;

import akkount.entity.Account;
import akkount.entity.Category;
import akkount.entity.Operation;
import akkount.entity.OperationType;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.screen.LoadDataBeforeShow;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.ui.screen.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@UiController("akk_Operation.lookup")
@UiDescriptor("operation-browse.xml")
@LookupComponent("operationTable")
@LoadDataBeforeShow
public class OperationBrowse extends StandardLookup<Operation> {

    @Inject
    protected Table<Operation> operationTable;

    @Inject
    protected Button createExpenseBtn;

    @Inject
    protected Button createIncomeBtn;

    @Inject
    protected Button createTransferBtn;
    @Inject
    private ScreenBuilders screenBuilders;
    @Inject
    private CollectionLoader<Operation> operationsDl;

    @Autowired
    private LookupField<Account> accFilterField;

    @Autowired
    private LookupField<Category> categoryFilterField;

    @Autowired
    private CheckBox showGenericFilterCheckbox;

    @Autowired
    private com.haulmont.cuba.gui.components.Filter filter;

    @Subscribe("operationTable.createExpense")
    private void onOperationTableCreateExpense(Action.ActionPerformedEvent event) {
        openEditorForCreate(OperationType.EXPENSE);
    }

    @Subscribe("operationTable.createIncome")
    private void onOperationTableCreateIncome(Action.ActionPerformedEvent event) {
        openEditorForCreate(OperationType.INCOME);
    }

    @Subscribe("operationTable.createTransfer")
    private void onOperationTableCreateTransfer(Action.ActionPerformedEvent event) {
        openEditorForCreate(OperationType.TRANSFER);
    }

    @Subscribe("accFilterField")
    public void onAccFilterFieldValueChange(HasValue.ValueChangeEvent<Account> event) {
        reload();
    }

    @Subscribe("categoryFilterField")
    public void onCategoryFilterFieldValueChange(HasValue.ValueChangeEvent<Category> event) {
        reload();
    }

    private void reload() {
        String join = "";
        String where = "";
        Map<String, Object> paramsMap = new HashMap<>();

        Account account = accFilterField.getValue();
        if (account != null) {
            join += " left join e.acc1 a1 left join e.acc2 a2 ";
            where += " and (a1.id = :account or a2.id = :account)";
            paramsMap.put("account", account.getId());
        }

        Category category = categoryFilterField.getValue();
        if (category != null) {
            where += " and (e.category = :category)";
            paramsMap.put("category", category);
        }

        String query = "select e from akk_Operation e " + join + " where (1=1) " + where + " order by e.opDate desc, e.createTs desc";
        operationsDl.setQuery(query);
        operationsDl.setParameters(paramsMap);
        operationsDl.load();
    }

    @Subscribe("showGenericFilterCheckbox")
    public void onShowGenericFilterCheckboxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        filter.setVisible(Boolean.TRUE.equals(showGenericFilterCheckbox.getValue()));
    }

    private void openEditorForCreate(OperationType operationType) {
        Account account = accFilterField.getValue();

        OperationEdit editor = screenBuilders.editor(operationTable)
                .withScreenClass(OperationEdit.class)
                .newEntity()
                .withInitializer(operation -> operation.setOpType(operationType))
                .build();
        editor.setAccountByFilter(account);
        editor.addAfterCloseListener(afterCloseEvent -> {
            if (afterCloseEvent.getCloseAction().equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                operationsDl.load();
            }
        });
        editor.show();
    }

    @Subscribe("operationTable.edit")
    private void onOperationTableEdit(Action.ActionPerformedEvent event) {
        Screen editor = screenBuilders.editor(operationTable).build();
        editor.addAfterCloseListener(afterCloseEvent -> {
            if (afterCloseEvent.getCloseAction().equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                operationsDl.load();
            }
        });
        editor.show();
    }


//    @Override
//    public void init(Map<String, Object> params) {
//        OperationCreateAction createExpenseAction = new OperationCreateAction(OperationType.EXPENSE);
//        operationTable.addAction(createExpenseAction);
//        createExpenseBtn.setAction(createExpenseAction);
//
//        OperationCreateAction createIncomeAction = new OperationCreateAction(OperationType.INCOME);
//        operationTable.addAction(createIncomeAction);
//        createIncomeBtn.setAction(createIncomeAction);
//
//        OperationCreateAction createTransferAction = new OperationCreateAction(OperationType.TRANSFER);
//        operationTable.addAction(createTransferAction);
//        createTransferBtn.setAction(createTransferAction);
//
//        operationTable.addAction(new OperationEditAction());
//    }

//    protected class OperationCreateAction extends CreateAction {
//
//        public OperationCreateAction(OperationType opType) {
//            super(OperationBrowse.this.operationTable, WindowManager.OpenType.NEW_TAB, opType.name());
//            setInitialValues(Collections.<String, Object>singletonMap("opType", opType));
//            setCaption(messages.getMessage(opType));
//            setShortcut("Ctrl-Shift-Key" + String.valueOf(opType.ordinal() + 1));
//        }
//
//        @Override
//        protected void afterCommit(Entity entity) {
//            operationTable.getDatasource().refresh();
//        }
//    }
//
//    protected class OperationEditAction extends EditAction {
//
//        public OperationEditAction() {
//            super(OperationBrowse.this.operationTable, WindowManager.OpenType.NEW_TAB);
//        }
//
//        @Override
//        protected void afterCommit(Entity entity) {
//            operationTable.getDatasource().refresh();
//        }
//    }
}