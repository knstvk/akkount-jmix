package akkount.service;

import akkount.entity.*;
import akkount.event.BalanceChangedEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.flowui.UiEventPublisher;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SampleDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(SampleDataGenerator.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected BalanceService balanceService;

    @Autowired
    private Metadata metadata;

    @Autowired
    private UiEventPublisher uiEventPublisher;

    @Autowired
    private OperationListener operationListener;

    @Autowired
    private DataManager dataManager;

    private class Context {

        Currency rubCurrency;
        Currency eurCurrency;

        List<Account> accounts = new ArrayList<>();

        List<Category> expenseCategories = new ArrayList<>();

        Category salaryCategory;
        Category otherIncomeCategory;
    }

    private Map<String, BigDecimal> currencyRates = new HashMap<>();

    public SampleDataGenerator() {
        currencyRates.put("rub", BigDecimal.ONE);
        currencyRates.put("eur", new BigDecimal("70"));
    }

    public String generateSampleData(int numberOfDaysBack) {
        if (numberOfDaysBack < 1 || numberOfDaysBack > 1000) {
            return "numberOfDaysBack must be between 1 and 1000";
        }
        LocalDate startDate = timeSource.now().toLocalDate().minusDays(numberOfDaysBack);

        try {
            Context context = new Context();
            createCurrencies(context);
            createAccounts(context);
            createCategories(context);
            createOperations(startDate, numberOfDaysBack, context);

            return "Done";
        } catch (Throwable e) {
            log.error("Error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public String removeAllData(String confirm) {
        if (!"ok".equals(confirm)) {
            return "Pass 'ok' in the parameter";
        }

        try {
            cleanupTable("AKK_OPERATION");
            cleanupTable("AKK_BALANCE");
            cleanupTable("AKK_ACCOUNT");
            cleanupTable("AKK_CATEGORY");
            cleanupTable("AKK_CURRENCY");

            uiEventPublisher.publishEvent(new BalanceChangedEvent(this));

            return "Done";
        } catch (Throwable e) {
            log.error("Error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    private void cleanupTable(String table) throws SQLException {
        jdbcTemplate.update("delete from " + table);
    }

    private void createCurrencies(final Context context) {
        Currency currency = metadata.create(Currency.class);
        currency.setCode("rub");
        currency.setName("Russian Rubles");
        dataManager.save(currency);
        context.rubCurrency = currency;

        currency = metadata.create(Currency.class);
        currency.setCode("eur");
        currency.setName("Euro");
        dataManager.save(currency);
        context.eurCurrency = currency;
    }

    private void createAccounts(final Context context) {
        Account account;

        account = metadata.create(Account.class);
        account.setName("Credit card");
        account.setCurrency(context.rubCurrency);
        account.setGroup(1);
        dataManager.save(account);
        context.accounts.add(account);

        account = metadata.create(Account.class);
        account.setName("Cash");
        account.setCurrency(context.rubCurrency);
        account.setGroup(1);
        dataManager.save(account);
        context.accounts.add(account);

        account = metadata.create(Account.class);
        account.setName("Deposit");
        account.setCurrency(context.rubCurrency);
        account.setGroup(2);
        dataManager.save(account);
        context.accounts.add(account);

        account = metadata.create(Account.class);
        account.setName("Deposit EUR");
        account.setCurrency(context.eurCurrency);
        account.setGroup(2);
        dataManager.save(account);
        context.accounts.add(account);
    }

    private void createCategories(final Context context) {
        Category category;

        category = metadata.create(Category.class);
        category.setName("Housekeeping");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Hobby");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Travel");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Food");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Clothes");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Car");
        category.setCatType(CategoryType.EXPENSE);
        dataManager.save(category);
        context.expenseCategories.add(category);

        category = metadata.create(Category.class);
        category.setName("Salary");
        category.setCatType(CategoryType.INCOME);
        dataManager.save(category);
        context.salaryCategory = category;

        category = metadata.create(Category.class);
        category.setName("Other");
        category.setCatType(CategoryType.INCOME);
        dataManager.save(category);
        context.otherIncomeCategory = category;
    }

    private void createOperations(LocalDate startDate, int numberOfDays, Context context) {
        operationListener.enableBalanceChangedEvents(false);
        try {
            for (int i = 0; i < numberOfDays; i++) {
                LocalDate date = startDate.plusDays(i);

                if (i % 7 == 0) {
                    income(date, context.accounts.get(0), context.salaryCategory, new BigDecimal("100000"));
                }
                if (i % 5 == 0) {
                    income(date, context.accounts.get(1), context.otherIncomeCategory, new BigDecimal(1000 + Math.round(Math.random() * 5000)));
                }

                for (int j = 0; j < Math.random() * 7; j++) {
                    expense(date, context.accounts.get(1), context);
                    if (j % 2 == 0)
                        expense(date, context.accounts.get(0), context);
                }

                if (i % 2 == 0) {
                    Account account2 = context.accounts.get((int) (1 + Math.random() * (context.accounts.size() - 1)));
                    transfer(date, context.accounts.get(0), account2);
                }

            }
        } finally {
            operationListener.enableBalanceChangedEvents(true);
        }
        // todo sending UI events
//        uiEventPublisher.publishEvent(new BalanceChangedEvent(this));
    }

    private void income(final LocalDate date, final Account account, final Category category, final BigDecimal amount) {
        Operation operation = metadata.create(Operation.class);
        operation.setOpType(OperationType.INCOME);
        operation.setOpDate(date);
        operation.setAcc2(account);
        operation.setCategory(category);
        operation.setAmount2(amount);
        dataManager.save(operation);

        log.info("Income: " + date + ", " + account.getName() + ", " + amount);
    }

    private void expense(final LocalDate date, final Account account, final Context context) {
        int categoryIdx = (int) Math.round(Math.random() * (context.expenseCategories.size() - 1));
        Category category = context.expenseCategories.get(categoryIdx);
        if (category == null)
            return;

        int categoryWeight = context.expenseCategories.size() - categoryIdx;
        BigDecimal amount = randomExpenseAmount(account, date, 0.1 + (categoryWeight * 0.05));
        if (BigDecimal.ZERO.compareTo(amount) >= 0)
            return;

        Operation operation = metadata.create(Operation.class);
        operation.setOpType(OperationType.EXPENSE);
        operation.setOpDate(date);
        operation.setAcc1(account);
        operation.setCategory(category);
        operation.setAmount1(amount);
        dataManager.save(operation);

        log.info("Expense: " + date + ", " + account.getName() + ", " + amount);
    }

    private void transfer(final LocalDate date, final Account account1, final Account account2) {
        BigDecimal amount1 = randomExpenseAmount(account1, date, 0.5);
        if (BigDecimal.ZERO.compareTo(amount1) >= 0)
            return;

        BigDecimal amount2 = transferAmount(account1, account2, amount1);

        Operation operation = metadata.create(Operation.class);
        operation.setOpType(OperationType.TRANSFER);
        operation.setOpDate(date);
        operation.setAcc1(account1);
        operation.setAmount1(amount1);
        operation.setAcc2(account2);
        operation.setAmount2(amount2);
        dataManager.save(operation);

        log.info("Transfer: " + date + ", " + account1.getName() + ", " + amount1+ ", " + account2.getName() + ", " + amount2);
    }


    private BigDecimal randomExpenseAmount(Account account, LocalDate date, Double part) {
        BigDecimal balance = balanceService.getBalance(account.getId(), date);
        if (BigDecimal.ZERO.compareTo(balance) >= 0)
            return BigDecimal.ZERO;
        else {
            return new BigDecimal((int) (Math.random() * balance.doubleValue() * part));
        }
    }

    private BigDecimal transferAmount(Account account1, Account account2, BigDecimal amount1) {
        if (account1.getCurrency().equals(account2.getCurrency()))
            return amount1;

        BigDecimal rate1 = currencyRates.get(account1.getCurrencyCode());
        BigDecimal rate2 = currencyRates.get(account2.getCurrencyCode());
        return amount1.multiply(rate1).divide(rate2, 0, RoundingMode.HALF_UP);
    }
}
