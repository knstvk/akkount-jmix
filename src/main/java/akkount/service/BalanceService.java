package akkount.service;

import akkount.entity.Account;
import akkount.entity.Balance;
import akkount.entity.Currency;
import akkount.entity.Operation;
import io.jmix.core.DataManager;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PolicyStore policyStore;

    @Autowired
    private SecureOperations secureOperations;

    @Autowired
    private CurrencyRatesService currencyRatesService;

    @Transactional
    public BigDecimal getBalance(final UUID accountId, final LocalDate date) {
        if (!secureOperations.isSpecificPermitted("get-balance", policyStore)) {
            return BigDecimal.ZERO;
        }

        List<Balance> balanceList = dataManager.load(Balance.class)
                .query("e.account.id = ?1 and e.balanceDate < ?2 order by e.balanceDate desc", accountId, date)
                .maxResults(1)
                .list();

        Balance startBalance = balanceList.isEmpty() ? null : balanceList.get(0);
        BigDecimal startAmount = startBalance != null ? startBalance.getAmount() : BigDecimal.ZERO;

        Map<String, Object> params = new HashMap<>();
        String expenseQueryStr = "select sum(o.amount1) from akk_Operation o where o.acc1.id = :accountId and o.opDate <= :date";
        params.put("accountId", accountId);
        params.put("date", date);
        if (startBalance != null) {
            expenseQueryStr += " and o.opDate >= :startBalanceDate";
            params.put("startBalanceDate", startBalance.getBalanceDate());
        }

        BigDecimal expense = dataManager.loadValue(expenseQueryStr, BigDecimal.class)
                .setParameters(params)
                .optional().orElse(BigDecimal.ZERO);

        params = new HashMap<>();
        String incomeQueryStr = "select sum(o.amount2) from akk_Operation o where o.acc2.id = :accountId and o.opDate <= :date";
        params.put("accountId", accountId);
        params.put("date", date);
        if (startBalance != null) {
            incomeQueryStr += " and o.opDate >= :startBalanceDate";
            params.put("startBalanceDate", startBalance.getBalanceDate());
        }

        BigDecimal income = dataManager.loadValue(incomeQueryStr, BigDecimal.class)
                .setParameters(params)
                .optional().orElse(BigDecimal.ZERO);

        return startAmount.add(income).subtract(expense);
    }

    @Transactional
    public void recalculateBalance(UUID accountId) {
        removeBalanceRecords(accountId);

        TreeMap<LocalDate, Balance> balances = new TreeMap<>();

        List<Operation> operations = dataManager.load(Operation.class)
                .query("e.acc1.id = ?1 or e.acc2.id = ?1 order by e.opDate", accountId)
                .list();
        for (Operation operation : operations) {
            addOperation(balances, operation, accountId);
        }
        for (Balance balance : balances.values()) {
            dataManager.save(balance);
        }
    }

    public List<BalanceData> getBalanceData(LocalDate date) {
        Map<Integer, List<Account>> accountsByGroup = dataManager.load(Account.class)
                .query("select e from akk_Account e " +
                        "where e.active = true " +
                        "order by e.group asc, e.name asc")
                .list().stream()
                .collect(Collectors.groupingBy(
                        account -> account.getGroup() != null ? account.getGroup() : 0,
                        TreeMap::new, Collectors.toList()));

        Optional<Currency> baseCurrency = dataManager.load(Currency.class)
                .query("select e from akk_Currency e where e.base = true")
                .maxResults(1)
                .optional();
        Optional<Map<String, BigDecimal>> rates = baseCurrency
                .map(currency -> currencyRatesService.getRates(currency.getCode()))
                .orElse(Optional.empty());

        List<BalanceData> result = new ArrayList<>(accountsByGroup.size());

        for (List<Account> accounts : accountsByGroup.values()) {
            Map<Account, BigDecimal> balanceByAccount = new LinkedHashMap<>();
            for (Account account : accounts) {
                BigDecimal balance = getBalance(account.getId(), date);
                if (BigDecimal.ZERO.compareTo(balance) != 0) {
                    balanceByAccount.put(account, balance);
                }
            }
            BalanceData.AccountBalance baseTotal = null;
            if (baseCurrency.isPresent() && rates.isPresent()) {
                baseTotal = calculateBaseTotal(balanceByAccount, baseCurrency.get().getCode(), rates.get());
            }
            BalanceData balanceData = new BalanceData(balanceByAccount, baseTotal);
            result.add(balanceData);
        }

        return result;
    }

    @Nullable
    private BalanceData.AccountBalance calculateBaseTotal(Map<Account, BigDecimal> balanceByAccount,
                                                          String baseCode,
                                                          Map<String, BigDecimal> rates) {
        if (balanceByAccount.isEmpty()) {
            return null;
        }
        Map<String, BigDecimal> balanceByCurrency = new TreeMap<>();
        for (Map.Entry<Account, BigDecimal> entry : balanceByAccount.entrySet()) {
            Account account = entry.getKey();
            BigDecimal val = balanceByCurrency.computeIfAbsent(account.getCurrencyCode(), s -> BigDecimal.ZERO);
            balanceByCurrency.put(account.getCurrencyCode(), val.add(entry.getValue()));
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : balanceByCurrency.entrySet()) {
            String currency = entry.getKey();
            BigDecimal rate;
            if (currency.equalsIgnoreCase(baseCode)) {
                rate = BigDecimal.ONE;
            } else {
                rate = rates.get(currency.toLowerCase());
                if (rate == null) {
                    continue;
                }
            }
            total = total.add(entry.getValue().divide(rate, 2, RoundingMode.HALF_UP));
        }

        return new BalanceData.AccountBalance(null, null, baseCode, total);
    }

    private void removeBalanceRecords(UUID accountId) {
        List<Balance> list = dataManager.load(Balance.class)
                .query("e.account.id = ?1", accountId)
                .list();
        for (Balance balance : list) {
            dataManager.remove(balance);
        }
    }

    private void addOperation(TreeMap<LocalDate, Balance> balances, Operation operation, UUID accountId) {
        if (operation.getAcc1() != null && operation.getAcc1().getId().equals(accountId)) {
            Map.Entry<LocalDate, Balance> entry = balances.higherEntry(operation.getOpDate());
            if (entry == null) {
                Balance balance = dataManager.create(Balance.class);
                balance.setAccount(operation.getAcc1());
                balance.setAmount(operation.getAmount1().negate()
                        .add(previousBalanceAmount(balances, operation.getOpDate())));
                balance.setBalanceDate(operation.getOpDate().with(TemporalAdjusters.lastDayOfMonth()));
                balances.put(balance.getBalanceDate(), balance);
            } else {
                Balance balance = entry.getValue();
                balance.setAmount(balance.getAmount().subtract(operation.getAmount1()));
            }
        }
        if (operation.getAcc2() != null && operation.getAcc2().getId().equals(accountId)) {
            Map.Entry<LocalDate, Balance> entry = balances.higherEntry(operation.getOpDate());
            if (entry == null) {
                Balance balance = dataManager.create(Balance.class);
                balance.setAccount(operation.getAcc2());
                balance.setAmount(operation.getAmount2()
                        .add(previousBalanceAmount(balances, operation.getOpDate())));
                balance.setBalanceDate(operation.getOpDate().with(TemporalAdjusters.lastDayOfMonth()));
                balances.put(balance.getBalanceDate(), balance);
            } else {
                Balance balance = entry.getValue();
                balance.setAmount(balance.getAmount().add(operation.getAmount2()));
            }
        }
    }

    private BigDecimal previousBalanceAmount(TreeMap<LocalDate, Balance> balances, LocalDate opDate) {
        Map.Entry<LocalDate, Balance> entry = balances.floorEntry(opDate);
        return entry == null ? BigDecimal.ZERO : entry.getValue().getAmount();
    }
}
