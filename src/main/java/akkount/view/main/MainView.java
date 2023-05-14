package akkount.view.main;

import akkount.event.BalanceChangedEvent;
import akkount.service.BalanceData;
import akkount.service.BalanceService;
import akkount.view.DecimalFormatter;
import akkount.view.preferences.PreferencesView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import io.jmix.core.TimeSource;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.Date;
import java.util.List;

@Route("")
@ViewController("MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {

    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    @ViewComponent
    private VerticalLayout balanceGroupsBox;

    @Autowired
    private BalanceService balanceService;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DecimalFormatter decimalFormatter;
    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe
    public void onInit(InitEvent event) {
        refreshBalance();
    }

    @EventListener(BalanceChangedEvent.class)
    public void refreshBalance() {
        log.info("Refreshing balance");

        balanceGroupsBox.removeAll();

        Date currentDate = timeSource.currentTimestamp();

        List<BalanceData> balanceDataList = balanceService.getBalanceData(currentDate);

        if (!balanceDataList.isEmpty()) {
            for (int i = 0; i < balanceDataList.size(); i++) {
                BalanceData balanceData = balanceDataList.get(i);
                Div groupDiv = createBalanceGroup(balanceData);

                Details details = uiComponents.create(Details.class);
                details.setClassName("balance-details");
                details.setSummaryText("Balance" + (balanceDataList.size() > 1 ? " [" + (i+1) + "]" : ""));
                details.setContent(groupDiv);
                if (i == 0)
                    details.setOpened(true);

                balanceGroupsBox.add(details);
            }
        }

    }

    private Div createBalanceGroup(BalanceData balanceData) {
        Div groupDiv = uiComponents.create(Div.class);
        groupDiv.setClassName("balance-container");

        for (BalanceData.AccountBalance balance : balanceData.accounts) {
            Element div = ElementFactory.createDiv();
            div.setAttribute("class", "balance-line balance-account-line");

            Element nameDiv = ElementFactory.createDiv();
            nameDiv.setText(balance.name);
            nameDiv.setAttribute("class", "balance-account");
            div.appendChild(nameDiv);

            Element amountDiv = ElementFactory.createDiv();
            amountDiv.setText(decimalFormatter.apply(balance.amount));
            amountDiv.setAttribute("class", "balance-amount");
            div.appendChild(amountDiv);

            Element currencyDiv = ElementFactory.createDiv();
            currencyDiv.setText(balance.currency);
            currencyDiv.setAttribute("class", "balance-currency");
            div.appendChild(currencyDiv);

            groupDiv.getElement().appendChild(div);
        }

        for (BalanceData.AccountBalance balance : balanceData.totals) {
            Element div = ElementFactory.createDiv();
            div.setAttribute("class", "balance-line balance-total-line");

            Element nameDiv = ElementFactory.createDiv();
            nameDiv.setText("");
            nameDiv.setAttribute("class", "balance-account");
            div.appendChild(nameDiv);

            Element amountDiv = ElementFactory.createDiv();
            amountDiv.setText(decimalFormatter.apply(balance.amount));
            amountDiv.setAttribute("class", "balance-amount");
            div.appendChild(amountDiv);

            Element currencyDiv = ElementFactory.createDiv();
            currencyDiv.setText(balance.currency);
            currencyDiv.setAttribute("class", "balance-currency");
            div.appendChild(currencyDiv);

            groupDiv.getElement().appendChild(div);
        }

        return groupDiv;
    }

    @Subscribe("preferencesBtn")
    public void onPreferencesBtnClick(ClickEvent<Button> event) {
        viewNavigators.view(PreferencesView.class).navigate();
    }
}
