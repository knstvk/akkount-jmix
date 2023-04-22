package akkount.view.operation;

import akkount.entity.Account;
import akkount.entity.Category;
import akkount.entity.Operation;
import akkount.view.main.MainView;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.facet.QueryParametersFacet;
import io.jmix.flowui.facet.queryparameters.AbstractQueryParametersBinder;
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

    private static final String ACCOUNT_URL_PARAM = "account";
    private static final String CATEGORY_URL_PARAM = "category";
    private static final String FILTER_OPENED_URL_PARAM = "filterOpened";

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
    private QueryParametersFacet queryParameters;

    private boolean entering;

    @Subscribe
    public void onInit(InitEvent event) {
        queryParameters.registerBinder(new SimpleFilterBinder());
    }

    @Subscribe("accFilterField")
    public void onAccFilterFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<Account>, Account> event) {
        if (!entering) {
            reload();
        }
    }

    @Subscribe("categoryFilterField")
    public void onCategoryFilterFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<Account>, Account> event) {
        if (!entering) {
            reload();
        }
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

    private class SimpleFilterBinder extends AbstractQueryParametersBinder {

        public SimpleFilterBinder() {
            genericFilter.addOpenedChangeListener(event -> {
                boolean opened = event.isOpened();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(FILTER_OPENED_URL_PARAM,
                        opened ? Collections.singletonList("1") : Collections.emptyList()));
                fireQueryParametersChanged(new QueryParametersFacet.QueryParametersChangeEvent(this, qp));
            });
            accFilterField.addValueChangeListener(event -> {
                Account account = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(ACCOUNT_URL_PARAM,
                        account == null ? Collections.emptyList() : Collections.singletonList(account.getId().toString())));
                fireQueryParametersChanged(new QueryParametersFacet.QueryParametersChangeEvent(this, qp));
            });
            categoryFilterField.addValueChangeListener(event -> {
                Category category = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(CATEGORY_URL_PARAM,
                        category == null ? Collections.emptyList() : Collections.singletonList(category.getId().toString())));
                fireQueryParametersChanged(new QueryParametersFacet.QueryParametersChangeEvent(this, qp));
            });
        }

        @Override
        public void updateState(QueryParameters queryParameters) {
            List<String> strings = queryParameters.getParameters().get(FILTER_OPENED_URL_PARAM);
            if (strings != null) {
                genericFilter.setOpened("1".equals(strings.get(0)));
            }

            Account account = null;
            Category category = null;

            List<String> accounts = queryParameters.getParameters().get(ACCOUNT_URL_PARAM);
            if (accounts != null) {
                String accountIdStr = accounts.get(0);
                if (!Strings.isNullOrEmpty(accountIdStr))
                    account = dataManager.load(Account.class).id(UUID.fromString(accountIdStr)).optional().orElse(null);
            }

            List<String> categories = queryParameters.getParameters().get(CATEGORY_URL_PARAM);
            if (categories != null) {
                String categoryIdStr = categories.get(0);
                if (!Strings.isNullOrEmpty(categoryIdStr))
                    category = dataManager.load(Category.class).id(UUID.fromString(categoryIdStr)).optional().orElse(null);
            }

            setFilterOnOperationsDl(account, category);

            entering = true;
            accFilterField.setValue(account);
            categoryFilterField.setValue(category);
            entering = false;
        }
    }
}
