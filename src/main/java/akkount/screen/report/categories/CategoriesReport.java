package akkount.screen.report.categories;

import akkount.entity.Category;
import akkount.entity.CategoryAmount;
import akkount.entity.CategoryType;
import akkount.entity.Currency;
import akkount.screen.operation.ShowOperations;
import akkount.service.ReportService;
import akkount.service.UserDataKeys;
import akkount.service.UserDataService;
import io.jmix.core.LoadContext;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

@UiController("categories-report")
@UiDescriptor("categories-report.xml")
public class CategoriesReport extends Screen {

    @Inject
    protected RadioButtonGroup categoryTypeGroup;
    @Inject
    protected EntityComboBox<Currency> currencyField;
    @Inject
    protected ComboBox<Integer> periodTypeField;
    @Inject
    protected DateField<Date> from1;
    @Inject
    protected DateField<Date> from2;
    @Inject
    protected DateField<Date> to1;
    @Inject
    protected DateField<Date> to2;
    @Inject
    protected Table table1;
    @Inject
    protected Table table2;
    @Inject
    protected TextField totalField1;
    @Inject
    protected TextField totalField2;
    @Inject
    protected BoxLayout excludedBox;
    @Inject
    protected CollectionContainer<CategoryAmount> ds1;
    @Inject
    protected CollectionContainer<CategoryAmount> ds2;
    @Inject
    protected CollectionContainer<Currency> currenciesDs;
    @Inject
    protected UiComponents componentsFactory;
    @Inject
    protected UserDataService userDataService;

    private boolean doNotRefresh;

    private Map<Category, BoxLayout> excludedCategories = new HashMap<>();

    @Autowired
    private CollectionLoader<Currency> currenciesDl;

    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private CollectionLoader<CategoryAmount> dl1;
    @Autowired
    private CollectionLoader<CategoryAmount> dl2;

    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private ReportService reportService;

    @Subscribe
    public void onInit(InitEvent event) {
        initCurrencies();
        initCategoryTypes();
        initPeriodTypes();
        initDates();
        initExcludedCategories();
        initShowOperationsActions();

        refreshDs1();
        refreshDs2();
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

    private void initCategoryTypes() {
        List<CategoryType> categoryTypes = new ArrayList<>();
        categoryTypes.add(CategoryType.EXPENSE);
        categoryTypes.add(CategoryType.INCOME);
        categoryTypeGroup.setOptionsList(categoryTypes);
        categoryTypeGroup.setValue(CategoryType.EXPENSE);

        categoryTypeGroup.addValueChangeListener(event -> {
            refreshDs1();
            refreshDs2();
        });
    }

    private void initPeriodTypes() {
        Map<String, Integer> options = new LinkedHashMap<>();
        options.put(messageBundle.getMessage("1month"), 1);
        options.put(messageBundle.getMessage("2months"), 2);
        options.put(messageBundle.getMessage("3months"), 3);
        options.put(messageBundle.getMessage("6months"), 6);
        options.put(messageBundle.getMessage("12months"), 12);

        periodTypeField.setOptionsMap(options);
        periodTypeField.setValue(1);

        periodTypeField.addValueChangeListener(event -> {
            Integer months = event.getValue();
            if (months != null) {
                doNotRefresh = true;
                try {
                    Date end = to2.getValue();
                    from2.setValue(DateUtils.addMonths(end, -1 * months));
                    to1.setValue(DateUtils.addMonths(end, -1 * months));
                    from1.setValue(DateUtils.addMonths(end, -2 * months));
                } finally {
                    doNotRefresh = false;
                }
                refreshDs1();
                refreshDs2();
            }
        });
    }

    private void initDates() {
        Date now = new Date();

        from1.setValue(DateUtils.addMonths(now, -2));
        to1.setValue(DateUtils.addMonths(now, -1));

        from2.setValue(DateUtils.addMonths(now, -1));
        to2.setValue(now);

        Consumer<HasValue.ValueChangeEvent<Date>> period1Listener = event -> refreshDs1();
        from1.addValueChangeListener(period1Listener);
        to1.addValueChangeListener(period1Listener);

        Consumer<HasValue.ValueChangeEvent<Date>> period2Listener = event -> refreshDs2();
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

        table1.addAction(new ExcludeCategoryAction(table1));
        table2.addAction(new ExcludeCategoryAction(table2));
    }

    private void initShowOperationsActions() {
        ShowOperationsAction action1 = new ShowOperationsAction(table1, from1, to1);
        table1.addAction(action1);
        table1.setItemClickAction(action1);

        ShowOperationsAction action2 = new ShowOperationsAction(table2, from2, to2);
        table2.addAction(action2);
        table2.setItemClickAction(action2);
    }

    private void refreshDs1() {
        if (doNotRefresh)
            return;

        dl1.setParameters(createDatasourceParams(from1.getValue(), to1.getValue()));
        dl1.load();

        totalField1.setEditable(true);
        totalField1.setValue(getTotalAmount(ds1));
        totalField1.setEditable(false);
    }

    private void refreshDs2() {
        if (doNotRefresh)
            return;

        dl2.setParameters(createDatasourceParams(from2.getValue(), to2.getValue()));
        dl2.load();

        totalField2.setEditable(true);
        totalField2.setValue(getTotalAmount(ds2));
        totalField2.setEditable(false);
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

    private void excludeCategory(Category category) {
        if (excludedCategories.containsKey(category))
            return;

        BoxLayout box = componentsFactory.create(HBoxLayout.class);
        box.setMargin(false, true, false, false);

        Label label = componentsFactory.create(Label.class);
        label.setValue(category.getName());
        label.setAlignment(Component.Alignment.MIDDLE_LEFT);
        box.add(label);

        LinkButton button = componentsFactory.create(LinkButton.class);
        button.setIcon("font-icon:REMOVE");
        button.setAction(new AbstractAction("") {
            @Override
            public void actionPerform(Component component) {
                for (Iterator<Map.Entry<Category, BoxLayout>> it = excludedCategories.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Category, BoxLayout> entry = it.next();
                    if (entry.getValue() == box) {
                        excludedBox.remove(box);
                        it.remove();
                        userDataService.removeEntity(UserDataKeys.CAT_REP_EXCLUDED_CATEGORIES, entry.getKey());
                        break;
                    }
                }
                refreshDs1();
                refreshDs2();
            }
        });
        box.add(button);

        excludedBox.add(box);
        excludedCategories.put(category, box);
        refreshDs1();
        refreshDs2();
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
        Date fromDate = (Date) params.get("from");
        Date toDate = (Date) params.get("to");
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

    private class ExcludeCategoryAction extends ItemTrackingAction {

        private Table table;

        public ExcludeCategoryAction(Table table) {
            super("excludeCategory");
            this.table = table;
        }

        @Override
        public void actionPerform(Component component) {
            CategoryAmount categoryAmount = (CategoryAmount) table.getSingleSelected();
            if (categoryAmount != null) {
                excludeCategory(categoryAmount.getCategory());
                userDataService.addEntity(UserDataKeys.CAT_REP_EXCLUDED_CATEGORIES, categoryAmount.getCategory());
            }
        }
    }

    private class ShowOperationsAction extends ItemTrackingAction {

        private Table table;
        private DateField<Date> from;
        private DateField<Date> to;

        public ShowOperationsAction(Table table, DateField from, DateField to) {
            super("showOperations");
            this.table = table;
            this.from = from;
            this.to = to;
        }

        @Override
        public void actionPerform(Component component) {
            CategoryAmount categoryAmount = (CategoryAmount) table.getSingleSelected();
            if (categoryAmount != null) {
                ShowOperations showOperationsScreen = screenBuilders.screen(table.getFrame().getFrameOwner())
                        .withScreenClass(ShowOperations.class)
                        .withOpenMode(OpenMode.NEW_TAB)
                        .build();
                showOperationsScreen.setParams(new ShowOperations.Params(
                        categoryAmount.getCategory(),
                        from.getValue(),
                        to.getValue(),
                        currencyField.getValue().getCode()
                ));
                showOperationsScreen.show();
            }
        }
    }

}