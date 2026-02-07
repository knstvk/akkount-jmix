package akkount;

import akkount.entity.*;
import akkount.service.BalanceData;
import akkount.service.BalanceService;
import akkount.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class BalanceServiceTest {

    UUID account1Id;
    UUID account2Id;

    @Autowired
    DataManager dataManager;
    
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    BalanceService balanceService;

    @Autowired
    RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        cleanupTable("AKK_OPERATION");
        cleanupTable("AKK_BALANCE");
        cleanupTable("AKK_ACCOUNT");
        cleanupTable("AKK_CATEGORY");
        cleanupTable("AKK_CURRENCY");
        initTestData();
    }

    private void initTestData() {
        Currency currency1 = dataManager.create(Currency.class);
        currency1.setCode("TST");
        currency1.setName("Test Currency");
        dataManager.save(currency1);

        Account account1 = dataManager.create(Account.class);
        account1.setCurrency(currency1);
        account1.setName("TestAccount1");
        dataManager.save(account1);
        account1Id = account1.getId();

        Account account2 = dataManager.create(Account.class);
        account2.setCurrency(currency1);
        account2.setName("TestAccount2");
        account2.setGroup(1);
        dataManager.save(account2);
        account2Id = account2.getId();
    }

    protected void cleanupTable(String table) {
        jdbcTemplate.update("delete from " + table);
    }

    private LocalDate date(String dateStr) {
        return LocalDate.parse(dateStr);
    }

    private UUID income(LocalDate day, BigDecimal amount) {
        return income(day, amount, account1Id);
    }

    private UUID income(LocalDate day, BigDecimal amount, UUID accountId) {
        Operation operation = dataManager.create(Operation.class);
        operation.setOpType(OperationType.INCOME);
        operation.setOpDate(day);
        operation.setAcc2(dataManager.getReference(Account.class, accountId));
        operation.setAmount2(amount);
        dataManager.save(operation);
        return operation.getId();
    }

    private UUID expense(LocalDate day, BigDecimal amount) {
        Operation operation = dataManager.create(Operation.class);
        operation.setOpType(OperationType.EXPENSE);
        operation.setOpDate(day);
        operation.setAcc1(dataManager.getReference(Account.class, account1Id));
        operation.setAmount1(amount);
        dataManager.save(operation);
        return operation.getId();
    }

    private UUID transfer(LocalDate day, BigDecimal amount) {
        Operation operation = dataManager.create(Operation.class);
        operation.setOpType(OperationType.INCOME);
        operation.setOpDate(day);
        operation.setAcc1(dataManager.getReference(Account.class, account1Id));
        operation.setAmount1(amount);
        operation.setAcc2(dataManager.getReference(Account.class, account2Id));
        operation.setAmount2(amount);
        dataManager.save(operation);
        return operation.getId();
    }

    private void expenseUpdate(UUID operationId, LocalDate day, BigDecimal amount) {
        Operation operation = dataManager.load(Operation.class).id(operationId)./*fetchPlan("operation-with-accounts").*/one();
        operation.setOpType(OperationType.EXPENSE);
        operation.setOpDate(day);
        operation.setAcc1(dataManager.getReference(Account.class, account1Id));
        operation.setAcc2(null);
        operation.setAmount1(amount);
        operation.setAmount2(BigDecimal.ZERO);
        dataManager.save(operation);
    }

    private void incomeUpdate(UUID operationId, LocalDate day, BigDecimal amount) {
        Operation operation = dataManager.load(Operation.class).id(operationId)./*fetchPlan("operation-with-accounts").*/one();
        operation.setOpType(OperationType.INCOME);
        operation.setOpDate(day);
        operation.setAcc1(null);
        operation.setAcc2(dataManager.getReference(Account.class, account1Id));
        operation.setAmount1(BigDecimal.ZERO);
        operation.setAmount2(amount);
        dataManager.save(operation);
    }

    private void removeOperation(UUID operationId) {
        Operation operation = dataManager.load(Operation.class).id(operationId).one();
        dataManager.remove(operation);
    }

    private static void checkEquality(BigDecimal expected, BigDecimal actual) {
        assertThat(actual).isEqualByComparingTo(expected);
    }

    private void checkBalanceRecord(LocalDate day, BigDecimal amount) {
        Balance balance = dataManager.load(Balance.class)
                .query("select b from akk_Balance b where b.account.id = :accId and b.balanceDate = :balDate")
                .parameter("accId", account1Id)
                .parameter("balDate", day)
                .one();
        checkEquality(amount, balance.getAmount());
    }

    @Test
    public void testGetBalance() throws Exception {
        BigDecimal balance = balanceService.getBalance(account1Id, date("2014-01-01"));
        assertThat(balance).isEqualTo(BigDecimal.ZERO);

        ///////////////////////////////////////////////////

        income(date("2014-01-01"), BigDecimal.TEN);

        checkBalanceRecord(date("2014-02-01"), BigDecimal.TEN);

        balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(BigDecimal.TEN, balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(BigDecimal.TEN, balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(BigDecimal.TEN, balance);

        ///////////////////////////////////////////////////

        UUID expenseId = expense(date("2014-01-01"), BigDecimal.ONE);

        checkBalanceRecord(date("2014-02-01"), new BigDecimal("9"));

        balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(new BigDecimal("9"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(new BigDecimal("9"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("9"), balance);

        ///////////////////////////////////////////////////

        UUID incomeId = income(date("2014-02-05"), BigDecimal.TEN);

        checkBalanceRecord(date("2014-03-01"), new BigDecimal("19"));

        balance = balanceService.getBalance(account1Id, date("2014-02-04"));
        checkEquality(new BigDecimal("9"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-05"));
        checkEquality(new BigDecimal("19"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("19"), balance);

        ///////////////////////////////////////////////////

        removeOperation(incomeId);

        checkBalanceRecord(date("2014-03-01"), new BigDecimal("9"));

        balance = balanceService.getBalance(account1Id, date("2014-02-05"));
        checkEquality(new BigDecimal("9"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("9"), balance);

        ///////////////////////////////////////////////////

        expenseUpdate(expenseId, date("2014-01-01"), new BigDecimal("2"));

        checkBalanceRecord(date("2014-02-01"), new BigDecimal("8"));
        checkBalanceRecord(date("2014-03-01"), new BigDecimal("8"));

        balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(new BigDecimal("8"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(new BigDecimal("8"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("8"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("8"), balance);

        ///////////////////////////////////////////////////

        incomeUpdate(expenseId, date("2014-01-01"), new BigDecimal("20"));

        checkBalanceRecord(date("2014-02-01"), new BigDecimal("30"));
        checkBalanceRecord(date("2014-03-01"), new BigDecimal("30"));

        balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(new BigDecimal("30"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(new BigDecimal("30"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("30"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("30"), balance);

        ///////////////////////////////////////////////////

        incomeUpdate(expenseId, date("2014-02-02"), new BigDecimal("20"));

        checkBalanceRecord(date("2014-02-01"), new BigDecimal("10"));
        checkBalanceRecord(date("2014-03-01"), new BigDecimal("30"));

        balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(new BigDecimal("10"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(new BigDecimal("10"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("30"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("30"), balance);
    }

    @Test
    public void testOperationOnFirstDayOfMonth() {
        BigDecimal balance;
        balance = balanceService.getBalance(account1Id, date("2014-01-01"));
        checkEquality(BigDecimal.ZERO, balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(BigDecimal.ZERO, balance);

        ///////////////////////////////////////////////////

        income(date("2014-01-10"), BigDecimal.TEN);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(BigDecimal.TEN, balance);

        ///////////////////////////////////////////////////

        expense(date("2014-01-30"), BigDecimal.ONE);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("9"), balance);

        ///////////////////////////////////////////////////

        expense(date("2014-02-01"), BigDecimal.ONE);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("8"), balance);

        ///////////////////////////////////////////////////

        expense(date("2014-02-02"), BigDecimal.ONE);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("7"), balance);
    }

    @Test
    public void testTransfer() throws Exception {
        BigDecimal balance;

        ///////////////////////////////////////////////////

        income(date("2014-01-10"), BigDecimal.TEN);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(BigDecimal.TEN, balance);

        ///////////////////////////////////////////////////

        transfer(date("2014-01-11"), BigDecimal.ONE);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("9"), balance);

        balance = balanceService.getBalance(account2Id, date("2014-02-02"));
        checkEquality(BigDecimal.ONE, balance);
    }

    @Test
    public void testMissedMonths() throws Exception {
        BigDecimal balance;

        ///////////////////////////////////////////////////

        income(date("2014-01-31"), BigDecimal.TEN);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(BigDecimal.TEN, balance);

        ///////////////////////////////////////////////////

        transfer(date("2014-05-07"), BigDecimal.TEN);

        balance = balanceService.getBalance(account1Id, date("2014-06-02"));
        checkEquality(BigDecimal.ZERO, balance);
    }

    @Test
    public void testRecalculateBalance() throws Exception {
        testGetBalance();

        balanceService.recalculateBalance(account1Id);

        BigDecimal balance = balanceService.getBalance(account1Id, date("2014-01-02"));
        checkEquality(new BigDecimal("10"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-01"));
        checkEquality(new BigDecimal("10"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-02-02"));
        checkEquality(new BigDecimal("30"), balance);

        balance = balanceService.getBalance(account1Id, date("2014-04-10"));
        checkEquality(new BigDecimal("30"), balance);
    }

    @Test
    public void testGetBalanceData() {
        income(date("2020-01-01"), BigDecimal.ONE, account1Id);
        income(date("2020-01-02"), BigDecimal.TEN, account2Id);

        List<BalanceData> balanceData = balanceService.getBalanceData(date("2020-01-03"));
        assertThat(balanceData.size()).isEqualTo(2);

        List<BalanceData.AccountBalance> totals1 = balanceData.get(0).totals;
        assertThat(totals1).hasSize(1);
        assertThat(totals1.get(0).amount).isEqualByComparingTo(BigDecimal.ONE);

        List<BalanceData.AccountBalance> totals2 = balanceData.get(1).totals;
        assertThat(totals2).hasSize(1);
        assertThat(totals2.get(0).amount).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    public void testGetBalanceDataBaseCurrency() {
        Currency eur = dataManager.create(Currency.class);
        eur.setCode("EUR");
        eur.setName("Euro");
        dataManager.save(eur);

        Account account1 = dataManager.load(Account.class).id(account1Id).one();
        account1.setCurrency(eur);
        dataManager.save(account1);

        Account account2 = dataManager.load(Account.class).id(account2Id).one();
        account2.setCurrency(eur);
        dataManager.save(account2);

        Currency usd = dataManager.create(Currency.class);
        usd.setCode("USD");
        usd.setName("US Dollar");
        usd.setBase(true);
        dataManager.save(usd);

        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"date\":\"2020-01-03\",\"usd\":{\"eur\":2.0}}", MediaType.APPLICATION_JSON));

        income(date("2020-01-01"), BigDecimal.ONE, account1Id);
        income(date("2020-01-02"), BigDecimal.TEN, account2Id);

        List<BalanceData> balanceData = balanceService.getBalanceData(date("2020-01-03"));

        assertThat(balanceData).hasSize(2);
        assertThat(balanceData.get(0).baseTotal).isNotNull();
        assertThat(balanceData.get(0).baseTotal.amount).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(balanceData.get(0).baseTotal.currency).isEqualTo("USD");

        assertThat(balanceData.get(1).baseTotal).isNotNull();
        assertThat(balanceData.get(1).baseTotal.amount).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(balanceData.get(1).baseTotal.currency).isEqualTo("USD");

        server.verify();
    }
}
