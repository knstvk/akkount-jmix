package akkount.view.operation;

import akkount.entity.*;
import akkount.service.UserDataKeys;
import akkount.service.UserDataService;
import akkount.view.main.MainView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.TimeSource;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Route(value = "operations/:id", layout = MainView.class)
@ViewController("akk_Operation.detail")
@ViewDescriptor("operation-detail-view.xml")
@EditedEntityContainer("operationDc")
public class OperationDetailView extends StandardDetailView<Operation> {

    public static final String ACCOUNT_URL_PARAM = "accountByFilter";

    public static final String LAST_OPERATION_DATE_ATTR = "lastOperationDate";

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private UserDataService userDataService;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ViewValidation viewValidation;

    @ViewComponent
    private Tabs typeTabs;
    @ViewComponent
    private EntityComboBox<Account> acc2Field;
    @ViewComponent
    private TypedTextField<BigDecimal> amount2Field;
    @ViewComponent
    private EntityComboBox<Account> acc1Field;
    @ViewComponent
    private TypedTextField<BigDecimal> amount1Field;
    @ViewComponent
    private EntityComboBox<Category> categoryField;
    @ViewComponent
    private CollectionLoader<Category> categoriesDl;
    @ViewComponent
    private TypedDatePicker<Date> opDateField;
    @ViewComponent
    private CollectionContainer<Category> categoriesDc;
    @ViewComponent
    private FormLayout form;
    @ViewComponent
    private Span weekDayText;
    @ViewComponent
    private JmixButton saveAndCloseBtn;
    @ViewComponent
    private JmixButton closeBtn;

    private Account accountByFilter;

    private Registration amount1ChangeListenerRegistration;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        List<String> strings = queryParameters.getParameters().get(ACCOUNT_URL_PARAM);
        if (strings != null) {
            String accountIdStr = strings.get(0);
            accountByFilter = dataManager.load(Account.class).id(UUID.fromString(accountIdStr)).optional().orElse(null);
        }
        super.beforeEnter(event);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        setShowSaveNotification(false);

        Operation operation = getEditedEntity();
        OperationType opType = operation.getOpType();
        if (!entityStates.isNew(operation)) {
            typeTabs.getChildren().forEach(component -> {
                if (component instanceof Tab tab) {
                    if (tab.getId().orElse("").equals(opType.name().toLowerCase() + "Tab")) {
                        typeTabs.setSelectedTab(tab);
                    } else {
                        tab.setVisible(false);
                    }
                }
            });
        }
        configureControls(opType);
        loadCategories(opType);

        // added programmatically because if set in XML, back navigation doesn't happen and parent list view doesn't open
        saveAndCloseBtn.addFocusShortcut(Key.ENTER, KeyModifier.META);
        closeBtn.addFocusShortcut(Key.ESCAPE);

        opDateField.focus();
    }

    @Subscribe("typeTabs")
    public void onTypeTabsSelectedChange(Tabs.SelectedChangeEvent event) {
        OperationType opType = getSelectedOpType();
        getEditedEntity().setOpType(opType);
        configureControls(opType);
        loadCategories(opType);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Operation> event) {
        Operation operation = event.getEntity();

        operation.setOpType(OperationType.EXPENSE);
        operation.setOpDate(loadDate());

        switch (operation.getOpType()) {
            case EXPENSE -> {
                if (accountByFilter != null) {
                    operation.setAcc1(accountByFilter);
                } else {
                    operation.setAcc1(loadAccount(UserDataKeys.OP_EXPENSE_ACCOUNT));
                }
            }
            case INCOME -> {
                if (accountByFilter != null) {
                    operation.setAcc2(accountByFilter);
                } else {
                    operation.setAcc2(loadAccount(UserDataKeys.OP_INCOME_ACCOUNT));
                }
            }
            case TRANSFER -> {
                if (accountByFilter != null) {
                    operation.setAcc1(accountByFilter);
                } else {
                    operation.setAcc1(loadAccount(UserDataKeys.OP_TRANSFER_EXPENSE_ACCOUNT));
                }
                operation.setAcc2(loadAccount(UserDataKeys.OP_TRANSFER_INCOME_ACCOUNT));
            }
        }
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        Operation operation = getEditedEntity();
        switch (operation.getOpType()) {
            case EXPENSE -> {
                operation.setAcc2(null);
                operation.setAmount2(null);
            }
            case INCOME -> {
                operation.setAcc1(null);
                operation.setAmount1(null);
            }
            case TRANSFER -> {
                operation.setCategory(null);
            }
        }
    }

    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        VaadinSession.getCurrent().setAttribute(LAST_OPERATION_DATE_ATTR, getEditedEntity().getOpDate());
    }

    @Subscribe("opDateField")
    public void onOpDateFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        LocalDate date = event.getValue();
        weekDayText.setText(date == null ? "" : date.format(DateTimeFormatter.ofPattern("EEEE")));
    }

    @Override
    public boolean hasUnsavedChanges() {
        return super.hasUnsavedChanges() && someFieldIsNotEmpty();
    }

    private boolean someFieldIsNotEmpty() {
        for (Component component : form.getChildren().toList()) {
            if (component.isVisible()
                    && component instanceof HasValue<?, ?> hasValue
                    && !hasValue.isEmpty())
                return true;
        }
        return false;
    }

    private void configureControls(OperationType opType) {
        switch (opType) {
            case EXPENSE -> {
                showExpenseFields(true);
                showIncomeFields(false);
                categoryField.setVisible(true);
                unregisterAmount1ChangeListener();
            }
            case INCOME -> {
                showExpenseFields(false);
                showIncomeFields(true);
                categoryField.setVisible(true);
                unregisterAmount1ChangeListener();
            }
            case TRANSFER -> {
                showExpenseFields(true);
                showIncomeFields(true);
                categoryField.setVisible(false);
                registerAmount1ChangeListener();
            }
        }
        // revalidate after changing "required" property of components
        viewValidation.validateUiComponents(getContent());
    }

    private void showExpenseFields(boolean enable) {
        acc1Field.setVisible(enable);
        acc1Field.setRequired(enable);
        amount1Field.setVisible(enable);
        amount1Field.setRequired(enable);
    }

    private void showIncomeFields(boolean enable) {
        acc2Field.setVisible(enable);
        acc2Field.setRequired(enable);
        amount2Field.setVisible(enable);
        amount2Field.setRequired(enable);
    }

    private void registerAmount1ChangeListener() {
        amount1ChangeListenerRegistration = amount1Field.addValueChangeListener(e -> {
            if (e.getValue() != null && StringUtils.isBlank(amount2Field.getValue())) {
                amount2Field.setValue(e.getValue());
            }
        });
    }

    private void unregisterAmount1ChangeListener() {
        if (amount1ChangeListenerRegistration != null) {
            amount1ChangeListenerRegistration.remove();
            amount1ChangeListenerRegistration = null;
        }
    }

    private OperationType getSelectedOpType() {
        Tab selectedTab = typeTabs.getSelectedTab();
        return switch (selectedTab.getId().orElse("")) {
            case "expenseTab" -> OperationType.EXPENSE;
            case "incomeTab" -> OperationType.INCOME;
            case "transferTab" -> OperationType.TRANSFER;
            default -> throw new IllegalStateException("Unexpected value: " + selectedTab.getId().orElse(""));
        };
    }

    private void loadCategories(OperationType opType) {
        String catType = switch (opType) {
            case EXPENSE -> CategoryType.EXPENSE.getId();
            case INCOME -> CategoryType.INCOME.getId();
            case TRANSFER -> "";
        };
        categoriesDl.setParameter("catType", catType);
        categoriesDl.load();

        if (!categoriesDc.getItems().contains(getEditedEntity().getCategory()))
            getEditedEntity().setCategory(null);
    }

    private Account loadAccount(String key) {
        return userDataService.loadEntity(key, Account.class);
    }

    private Date loadDate() {
        Date date = (Date) VaadinSession.getCurrent().getAttribute(LAST_OPERATION_DATE_ATTR);
        return date != null ? date : DateUtils.truncate(timeSource.currentTimestamp(), Calendar.DAY_OF_MONTH);
    }
}