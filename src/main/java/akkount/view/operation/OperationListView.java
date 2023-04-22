package akkount.view.operation;

import akkount.entity.Account;
import akkount.entity.Category;
import akkount.entity.Operation;
import akkount.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "operations", layout = MainView.class)
@ViewController("akk_Operation.list")
@ViewDescriptor("operation-list-view.xml")
@LookupComponent("operationsTable")
@DialogMode(width = "50em", height = "37.5em")
public class OperationListView extends StandardListView<Operation> {

    public static final String ACCOUNT_URL_PARAM = "account";
    public static final String CATEGORY_URL_PARAM = "category";

    private static final String[] FILTER_OPTIONS = {"Simple filter", "Generic filter"};

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private CollectionLoader<Operation> operationsDl;

    @ViewComponent
    private EntityComboBox<Account> accFilterField;
    @ViewComponent
    private EntityComboBox<Category> categoryFilterField;
    @ViewComponent
    private GenericFilter genericFilter;
    @ViewComponent
    private JmixRadioButtonGroup<String> filterSelector;
    @ViewComponent
    private HorizontalLayout simpleFilterBox;

    private boolean entering;

    @Subscribe
    public void onInit(InitEvent event) {
        filterSelector.setItems(Arrays.asList(FILTER_OPTIONS));
        filterSelector.setValue(FILTER_OPTIONS[0]);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Account account = null;
        Category category = null;

        QueryParameters queryParameters = event.getLocation().getQueryParameters();

        List<String> accounts = queryParameters.getParameters().get(ACCOUNT_URL_PARAM);
        if (accounts != null) {
            String accountIdStr = accounts.get(0);
            account = dataManager.load(Account.class).id(UUID.fromString(accountIdStr)).optional().orElse(null);
        }

        List<String> categories = queryParameters.getParameters().get(CATEGORY_URL_PARAM);
        if (categories != null) {
            String categoryIdStr = categories.get(0);
            category = dataManager.load(Category.class).id(UUID.fromString(categoryIdStr)).optional().orElse(null);
        }

        setFilterOnOperationsDl(account, category);

        entering = true;
        accFilterField.setValue(account);
        categoryFilterField.setValue(category);
        entering = false;

        super.beforeEnter(event);
    }

    @Subscribe("accFilterField")
    public void onAccFilterFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<Account>, Account> event) {
        if (!entering) {
            reload();
            updateUrl();
        }
    }

    @Subscribe("categoryFilterField")
    public void onCategoryFilterFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<Account>, Account> event) {
        if (!entering) {
            reload();
            updateUrl();
        }
    }

    private void updateUrl() {
        Account account = accFilterField.getValue();
        Category category = categoryFilterField.getValue();

        String originalUrl = RouteConfiguration.forSessionScope().getUrl(getClass());

        var map = new HashMap<String, String>();
        if (account != null)
            map.put("account", account.getId().toString());
        if (category != null)
            map.put("category", category.getId().toString());

        if (map.isEmpty()) {
            replaceUrl(originalUrl);
        } else {
            String queryString = QueryParameters.simple(map).getQueryString();
            replaceUrl(originalUrl + "?" + queryString);
        }
    }

    private void replaceUrl(String url) {
        getUI().ifPresent(ui ->
                ui.getPage().getHistory().replaceState(null, url));
    }

    private void reload() {
        setFilterOnOperationsDl(accFilterField.getValue(), categoryFilterField.getValue());
        operationsDl.load();
    }

    private void setFilterOnOperationsDl(Account account, Category category) {
        String join = "";
        String where = "";
        Map<String, Object> paramsMap = new HashMap<>();

        if (account != null) {
            join += " left join e.acc1 a1 left join e.acc2 a2 ";
            where += " and (a1.id = :account or a2.id = :account)";
            paramsMap.put("account", account.getId());
        }

        if (category != null) {
            where += " and (e.category = :category)";
            paramsMap.put("category", category);
        }

        String query = "select e from akk_Operation e " + join + " where (1=1) " + where + " order by e.opDate desc, e.createTs desc";
        operationsDl.setQuery(query);
        operationsDl.setParameters(paramsMap);
    }


    @Install(to = "operationsTable.create", subject = "queryParametersProvider")
    private QueryParameters operationsTableCreateQueryParametersProvider() {
        Account account = accFilterField.getValue();
        return account == null ?
                QueryParameters.empty() :
                QueryParameters.of(OperationDetailView.ACCOUNT_URL_PARAM, account.getId().toString());
    }

    @Subscribe("filterSelector")
    public void onFilterSelectorComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup, Object> event) {
        if (FILTER_OPTIONS[0].equals(filterSelector.getValue())) {
            simpleFilterBox.setVisible(true);

            // TODO is there a simpler way to reset the filter?
            Configuration configuration = genericFilter.getEmptyConfiguration();
            configuration.getRootLogicalFilterComponent().removeAll();
            configuration.setModified(false);
            genericFilter.setCurrentConfiguration(configuration);
            genericFilter.apply();

            genericFilter.setVisible(false);
        } else {
            simpleFilterBox.setVisible(false);
            accFilterField.setValue(null);
            categoryFilterField.setValue(null);

            genericFilter.setVisible(true);
        }
    }
}