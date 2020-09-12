package akkount.web;

import akkount.event.BalanceChangedEvent;
import akkount.service.BalanceData;
import akkount.service.BalanceData.AccountBalance;
import akkount.service.BalanceService;
import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.TimeSource;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.UiComponents;
import com.haulmont.cuba.gui.components.AbstractMainWindow;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.GridLayout;
import com.haulmont.cuba.gui.components.Label;
import io.jmix.ui.component.mainwindow.AppMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainWindow extends AbstractMainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    @Inject
    protected AppMenu mainMenu;

    @Inject
    private BoxLayout balanceLayout;

    @Inject
    private UiComponents uiComponents;

    private GridLayout balanceGrid;

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private FormatStringsRegistry formatStringsRegistry;

    @Override
    public void init(Map<String, Object> params) {
        mainMenu.focus();
        refreshBalance();
    }

    @EventListener(BalanceChangedEvent.class)
    public void refreshBalance() {
        log.info("Refreshing balance");
        BalanceService balanceService = AppBeans.get(BalanceService.class);
        Date currentDate = AppBeans.get(TimeSource.class).currentTimestamp();

        List<BalanceData> balanceDataList = balanceService.getBalanceData(currentDate);

        if (balanceGrid != null) {
            balanceLayout.remove(balanceGrid);
        }

        balanceGrid = uiComponents.create(GridLayout.class);

        if (!balanceDataList.isEmpty()) {
            Integer rows = balanceDataList.stream()
                    .map(balanceData -> balanceData.accounts.size() + balanceData.totals.size() + 2)
                    .reduce(0, Integer::sum);

            balanceGrid.setColumns(3);
            balanceGrid.setRows(rows);
            balanceGrid.setMargin(true);
            balanceGrid.setSpacing(true);

            DecimalFormatter formatter = new DecimalFormatter(currentAuthentication, formatStringsRegistry);

            int row = 0;
            for (Iterator<BalanceData> iterator = balanceDataList.iterator(); iterator.hasNext(); ) {
                BalanceData balanceData = iterator.next();
                for (AccountBalance accountBalance : balanceData.accounts) {
                    addAccountBalance(accountBalance, formatter, row++);
                }
                for (AccountBalance accountBalance : balanceData.totals) {
                    addAccountBalance(accountBalance, formatter, row++);
                }
                if (iterator.hasNext())
                    addSeparator("<hr>", row++);
            }

        } else {
            balanceGrid.setColumns(1);
            balanceGrid.setRows(1);

            Label<String> label = uiComponents.create(Label.TYPE_STRING);
            label.setValue("No data");
            balanceGrid.add(label, 0, 0);
        }
        balanceLayout.add(balanceGrid);
    }

    private void addAccountBalance(AccountBalance accountBalance, DecimalFormatter formatter, int row) {
        if (accountBalance.name != null) {
            Label<String> label = uiComponents.create(Label.TYPE_STRING);
            label.setValue(accountBalance.name);
            balanceGrid.add(label, 0, row);
        }

        Label<String> sumLabel = uiComponents.create(Label.TYPE_STRING);
        sumLabel.setValue(formatter.apply(accountBalance.amount));
        sumLabel.setAlignment(Alignment.MIDDLE_RIGHT);
        if (accountBalance.name == null) {
            sumLabel.setStyleName("totals");
        }
        balanceGrid.add(sumLabel, 1, row);

        Label<String> curLabel = uiComponents.create(Label.TYPE_STRING);
        curLabel.setValue(accountBalance.currency);
        if (accountBalance.name == null) {
            curLabel.setStyleName("totals");
        }
        balanceGrid.add(curLabel, 2, row);
    }

    private void addSeparator(String html, int row) {
        Label<String> label = uiComponents.create(Label.TYPE_STRING);
        label.setValue(html);
        label.setHtmlEnabled(true);
        label.setSizeFull();
        balanceGrid.add(label, 0, row, 2, row);
    }
}
