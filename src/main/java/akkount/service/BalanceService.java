package akkount.service;

import akkount.entity.Account;
import akkount.entity.Balance;
import akkount.entity.Operation;
import io.jmix.core.DataManager;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        List<BalanceData> result = new ArrayList<>(accountsByGroup.size());

        for (List<Account> accounts : accountsByGroup.values()) {
            Map<Account, BigDecimal> balanceByAccount = new LinkedHashMap<>();
            for (Account account : accounts) {
                BigDecimal balance = getBalance(account.getId(), date);
                if (BigDecimal.ZERO.compareTo(balance) != 0) {
                    balanceByAccount.put(account, balance);
                }
            }
            BalanceData balanceData = new BalanceData(balanceByAccount);
            result.add(balanceData);
        }

        return result;
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