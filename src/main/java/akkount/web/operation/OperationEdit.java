package akkount.web.operation;

import akkount.entity.Account;
import akkount.entity.Operation;
import akkount.entity.OperationType;
import akkount.service.UserDataKeys;
import akkount.service.UserDataService;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.screen.LoadDataBeforeShow;
import com.vaadin.server.VaadinSession;
import io.jmix.core.TimeSource;
import io.jmix.ui.Fragments;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

@UiController("akk_Operation.edit")
@UiDescriptor("operation-edit.xml")
@EditedEntityContainer("operationDc")
@LoadDataBeforeShow
public class OperationEdit extends StandardEditor<Operation> {

    public static final String LAST_OPERATION_DATE_ATTR = "lastOperationDate";

    @Inject
    private GroupBoxLayout frameContainer;

    @Inject
    private TimeSource timeSource;

    @Inject
    private UserDataService userDataService;

    @Autowired
    private UserSessionSource userSessionSource;

    private OperationFrame operationFrame;

    @Inject
    private Messages messages;

    @Inject
    private Fragments fragments;

    private Account accountByFilter;

    public void setAccountByFilter(Account accountByFilter) {
        this.accountByFilter = accountByFilter;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent beforeShowEvent) {
        getScreenData().loadAll();

        Operation operation = getEditedEntity();
        if (operation.getOpType() == null)
            operation.setOpType(OperationType.EXPENSE);
        frameContainer.setCaption(messages.getMessage(operation.getOpType()));

        String frameId = operation.getOpType().name().toLowerCase() + "-frame";

        operationFrame = (OperationFrame) fragments.create(this, frameId);
        operationFrame.postInit(getEditedEntity());
        frameContainer.add(operationFrame.getFragment());
    }

    @Subscribe
    protected void initNewItem(InitEntityEvent<Operation> initEntityEvent) {
        Operation operation = initEntityEvent.getEntity();
        operation.setOpDate(loadDate());
        switch (operation.getOpType()) {
            case EXPENSE:
                if (accountByFilter != null) {
                    operation.setAcc1(accountByFilter);
                } else {
                    operation.setAcc1(loadAccount(UserDataKeys.OP_EXPENSE_ACCOUNT));
                }
                break;
            case INCOME:
                if (accountByFilter != null) {
                    operation.setAcc2(accountByFilter);
                } else {
                    operation.setAcc2(loadAccount(UserDataKeys.OP_INCOME_ACCOUNT));
                }
                break;
            case TRANSFER:
                if (accountByFilter != null) {
                    operation.setAcc1(accountByFilter);
                } else {
                    operation.setAcc1(loadAccount(UserDataKeys.OP_TRANSFER_EXPENSE_ACCOUNT));
                }
                operation.setAcc2(loadAccount(UserDataKeys.OP_TRANSFER_INCOME_ACCOUNT));
                break;
        }
    }

    @Override
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = super.validateScreen();
        operationFrame.postValidate(validationErrors);
        return validationErrors;
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void postCommit(DataContext.PostCommitEvent postCommitEvent) {
        VaadinSession.getCurrent().setAttribute(LAST_OPERATION_DATE_ATTR, getEditedEntity().getOpDate());
    }

    private Account loadAccount(String key) {
        return userDataService.loadEntity(key, Account.class);
    }

    private Date loadDate() {
        Date date = (Date) VaadinSession.getCurrent().getAttribute(LAST_OPERATION_DATE_ATTR);
        return date != null ? date : DateUtils.truncate(timeSource.currentTimestamp(), Calendar.DAY_OF_MONTH);
    }
}
