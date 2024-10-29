package akkount.view.report;


import akkount.entity.Category;
import akkount.entity.CategoryAmount;
import akkount.entity.CategoryType;
import akkount.entity.Currency;
import akkount.service.ReportService;
import akkount.service.UserDataKeys;
import akkount.service.UserDataService;
import akkount.view.main.MainView;
import akkount.view.operation.ShowOperationsView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Route(value = "report", layout = MainView.class)
@ViewController("akk_ReportView")
@ViewDescriptor("report-view.xml")
public class ReportView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(ReportView.class);

    private boolean doNotRefresh;

    private Map<Category, HorizontalLayout> excludedCategories = new HashMap<>();

    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private ReportService reportService;
    @Autowired
    private UserDataService userDataService;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private JmixComboBox<Integer> periodTypeField;
    @ViewComponent
    private CollectionLoader<Currency> currenciesDl;
    @ViewComponent
    private JmixRadioButtonGroup<CategoryType> categoryTypeGroup;
    @ViewComponent
    private CollectionContainer<Currency> currenciesDs;
    @ViewComponent
    private EntityComboBox<Currency> currencyField;
    @ViewComponent
    private CollectionLoader<CategoryAmount> dl1;
    @ViewComponent
    private TypedTextField<BigDecimal> totalField1;
    @ViewComponent
    private CollectionLoader<CategoryAmount> dl2;
    @ViewComponent
    private TypedTextField<BigDecimal> totalField2;
    @ViewComponent
    private TypedDatePicker<LocalDate> from1;
    @ViewComponent
    private TypedDatePicker<LocalDate> to1;
    @ViewComponent
    private TypedDatePicker<LocalDate> from2;
    @ViewComponent
    private TypedDatePicker<LocalDate> to2;
    @ViewComponent
    private CollectionContainer<CategoryAmount> ds1;
    @ViewComponent
    private CollectionContainer<CategoryAmount> ds2;
    @ViewComponent
    private DataGrid<CategoryAmount> dataGrid1;
    @ViewComponent
    private DataGrid<CategoryAmount> dataGrid2;
    @ViewComponent
    private HorizontalLayout excludedBox;

    @Subscribe
    public void onInit(InitEvent event) {
        initCurrencies();
        initCategoryTypes();
        initPeriodTypes();
        initDates();
        initExcludedCategories();

        refreshDs1();
        refreshDs2();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        currenciesDl.load();
    }

    private void refreshDs1() {
        if (doNotRefresh)
            return;

        long start = System.currentTimeMillis();

        dl1.setParameters(createDatasourceParams(from1.getTypedValue(), to1.getTypedValue()));
        dl1.load();

        totalField1.setTypedValue(getTotalAmount(ds1));

        log.debug("Period 1 data refreshed in {} ms", System.currentTimeMillis() - start);
    }

    private void refreshDs2() {
        if (doNotRefresh)
            return;

        long start = System.currentTimeMillis();

        dl2.setParameters(createDatasourceParams(from2.getTypedValue(), to2.getTypedValue()));
        dl2.load();

        totalField2.setTypedValue(getTotalAmount(ds2));

        log.debug("Period 2 data refreshed in {} ms", System.currentTimeMillis() - start);
    }

    private Map<String, Object> createDatasourceParams(Object from, Object to) {
        Map<String, Object> params = new HashMap<>();
        params.put("from", from);
        params.put("to", to);
        params.put("categoryType", categoryTypeGroup.getValue());
        params.put("currency", currencyField.getValue());
        params.put("excludedCategories", excludedCategories.keySet());
        return params;
    }

    private BigDecimal getTotalAmount(CollectionContainer<CategoryAmount> datasource) {
        BigDecimal total = BigDecimal.ZERO;
        for (CategoryAmount ca : datasource.getItems()) {
            total = total.add(ca.getAmount());
        }
        return total;
    }

    private void initCurrencies() {
        currenciesDl.load();
        Currency currency = userDataService.loadEntity(UserDataKeys.CAT_REP_CURRENCY, Currency.class);
        if (currency == null) {
            Collection<Currency> currencies = currenciesDs.getItems();
            if (!currencies.isEmpty())
                currency = currencies.iterator().next();
        }
        currencyField.setValue(currency);

        currencyField.addValueChangeListener(event -> {
            refreshDs1();
            refreshDs2();
            userDataService.saveEntity(UserDataKeys.CAT_REP_CURRENCY, event.getValue());
        });
    }

    private void initPeriodTypes() {
        Map<Integer, String> options = new LinkedHashMap<>();
        options.put(1, messageBundle.getMessage("1month"));
        options.put(2, messageBundle.getMessage("2months"));
        options.put(3, messageBundle.getMessage("3months"));
        options.put(6, messageBundle.getMessage("6months"));
        options.put(12, messageBundle.getMessage("12months"));

        ComponentUtils.setItemsMap(periodTypeField, options);
        periodTypeField.setValue(1);

        periodTypeField.addValueChangeListener(event -> {
            Integer months = event.getValue();
            if (months != null) {
                doNotRefresh = true;
                try {
                    LocalDate end = to2.getTypedValue();
                    from2.setTypedValue(end.minusMonths(months));
                    to1.setTypedValue(end.minusMonths(months));
                    from1.setTypedValue(end.minusMonths(months * 2));
                } finally {
                    doNotRefresh = false;
                }
                refreshDs1();
                refreshDs2();
            }
        });
    }

    private void initCategoryTypes() {
        List<CategoryType> categoryTypes = new ArrayList<>();
        categoryTypes.add(CategoryType.EXPENSE);
        categoryTypes.add(CategoryType.INCOME);
        categoryTypeGroup.setItems(categoryTypes);
        categoryTypeGroup.setValue(CategoryType.EXPENSE);

        categoryTypeGroup.addValueChangeListener(event -> {
            refreshDs1();
            refreshDs2();
        });
    }

    private void initDates() {
        LocalDate now = LocalDate.now();

        from1.setTypedValue(now.minusMonths(2));
        to1.setTypedValue(now.minusMonths(1));

        from2.setTypedValue(now.minusMonths(1));
        to2.setTypedValue(now);

        HasValue.ValueChangeListener period1Listener = event -> refreshDs1();
        from1.addValueChangeListener(period1Listener);
        to1.addValueChangeListener(period1Listener);

        HasValue.ValueChangeListener period2Listener = event -> refreshDs2();
        from2.addValueChangeListener(period2Listener);
        to2.addValueChangeListener(period2Listener);
    }

    private void initExcludedCategories() {
        doNotRefresh = true;
        try {
            List<Category> categoryList = userDataService.loadEntityList(UserDataKeys.CAT_REP_EXCLUDED_CATEGORIES, Category.class);
            for (Category category : categoryList) {
                excludeCategory(category);
            }
        } finally {
            doNotRefresh = false;
        }
    }

    @Install(to = "dl1", target = Target.DATA_LOADER)
    private List<CategoryAmount> dl1LoadDelegate(LoadContext<CategoryAmount> loadContext) {
        return loadData(loadContext.getQuery().getParameters());
    }

    @Install(to = "dl2", target = Target.DATA_LOADER)
    private List<CategoryAmount> dl2LoadDelegate(LoadContext<CategoryAmount> loadContext) {
        return loadData(loadContext.getQuery().getParameters());
    }

    protected List<CategoryAmount> loadData(Map<String, Object> params) {
        LocalDate fromDate = (LocalDate) params.get("from");
        LocalDate toDate = (LocalDate) params.get("to");
        if (fromDate == null || toDate == null || toDate.compareTo(fromDate) < 0)
            return Collections.emptyList();

        Currency currency = (Currency) params.get("currency");
        if (currency == null)
            Collections.emptyList();

        CategoryType categoryType = (CategoryType) params.get("categoryType");
        if (categoryType == null)
            categoryType = CategoryType.EXPENSE;

        //noinspection unchecked
        Set<Category> excludedCategories = (Set) params.get("excludedCategories");
        if (excludedCategories == null)
            excludedCategories = new HashSet<>();
        List<UUID> ids = new ArrayList<>(excludedCategories.size());
        for (Category category : excludedCategories) {
            ids.add(category.getId());
        }

        List<CategoryAmount> list = reportService.getTurnoverByCategories(fromDate, toDate, categoryType,
                currency.getCode(), ids);
        return list;
    }

    private void excludeCategory(Category category) {
        if (excludedCategories.containsKey(category))
            return;

        HorizontalLayout box = uiComponents.create(HorizontalLayout.class);
        box.setAlignItems(FlexComponent.Alignment.BASELINE);
        box.setSpacing(false);

        Span span = uiComponents.create(Span.class);
        span.setText(category.getName());
        box.add(span);

        JmixButton button = uiComponents.create(JmixButton.class);
        button.setIcon(new Icon("lumo", "cross"));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.addClickListener(event -> {
            for (Iterator<Map.Entry<Category, HorizontalLayout>> it = excludedCategories.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Category, HorizontalLayout> entry = it.next();
                if (entry.getValue() == box) {
                    excludedBox.remove(box);
                    it.remove();
                    userDataService.removeEntity(UserDataKeys.CAT_REP_EXCLUDED_CATEGORIES, entry.getKey());
                    break;
                }
            }
            refreshDs1();
            refreshDs2();
        });
        box.add(button);

        excludedBox.add(box);
        excludedCategories.put(category, box);
        refreshDs1();
        refreshDs2();
    }

    @Subscribe("excludeBtn1")
    public void onExcludeBtn1Click(ClickEvent<JmixButton> event) {
        excludeCategory(dataGrid1.getSingleSelectedItem());
    }

    @Subscribe("excludeBtn2")
    public void onExcludeBtn2Click(ClickEvent<JmixButton> event) {
        excludeCategory(dataGrid2.getSingleSelectedItem());
    }

    @Subscribe("dataGrid1.exclude")
    public void onDataGrid1Exclude(ActionPerformedEvent event) {
        excludeCategory(dataGrid1.getSingleSelectedItem());
    }

    @Subscribe("dataGrid2.exclude")
    public void onDataGrid2Exclude(ActionPerformedEvent event) {
        excludeCategory(dataGrid2.getSingleSelectedItem());
    }

    private void excludeCategory(CategoryAmount categoryAmount) {
        if (categoryAmount != null) {
            excludeCategory(categoryAmount.getCategory());
            userDataService.addEntity(UserDataKeys.CAT_REP_EXCLUDED_CATEGORIES, categoryAmount.getCategory());
        }
    }

    @Subscribe("dataGrid1.showOperations")
    public void onDataGrid1ShowOperations(ActionPerformedEvent event) {
        showOperations(dataGrid1.getSingleSelectedItem(), from1.getTypedValue(), to1.getTypedValue());
    }

    @Subscribe("showOperationsBtn1")
    public void onShowOperationsBtn1Click(ClickEvent<JmixButton> event) {
        showOperations(dataGrid1.getSingleSelectedItem(), from1.getTypedValue(), to1.getTypedValue());
    }

    @Subscribe("dataGrid2.showOperations")
    public void onDataGrid2ShowOperations(ActionPerformedEvent event) {
        showOperations(dataGrid2.getSingleSelectedItem(), from2.getTypedValue(), to2.getTypedValue());
    }

    @Subscribe("showOperationsBtn2")
    public void onShowOperationsBtn2Click(ClickEvent<JmixButton> event) {
        showOperations(dataGrid2.getSingleSelectedItem(), from2.getTypedValue(), to2.getTypedValue());
    }

    private void showOperations(CategoryAmount categoryAmount, LocalDate from, LocalDate to) {
        if (categoryAmount != null) {
            DialogWindow<ShowOperationsView> window = dialogWindows.view(this, ShowOperationsView.class).build();
            window.getView().setParams(new ShowOperationsView.Params(
                    categoryAmount.getCategory(),
                    from,
                    to,
                    currencyField.getValue().getCode()
            ));
            window.setResizable(true);
            window.setWidth("80%");
            window.open();
        }
    }
}