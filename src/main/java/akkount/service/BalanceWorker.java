package akkount.service;

import akkount.entity.Balance;
import akkount.entity.Operation;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanRepository;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Component(BalanceWorker.NAME)
public class BalanceWorker {

    public static final String NAME = "akk_BalanceWorker";

    @Autowired
    protected DataManager dataManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PolicyStore policyStore;

    @Autowired
    private SecureOperations secureOperations;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Transactional
    public BigDecimal getBalance(final UUID accountId, final LocalDate date) {
        if (!secureOperations.isSpecificPermitted("get-balance", policyStore)) {
            return BigDecimal.ZERO;
        }

        TypedQuery<Balance> balQuery = entityManager.createQuery(
                "select b from akk_Balance b where b.account.id = ?1 and b.balanceDate < ?2 order by b.balanceDate desc",
                Balance.class);
        balQuery.setParameter(1, accountId);
        balQuery.setParameter(2, date);
        balQuery.setMaxResults(1);
        List<Balance> balanceList = balQuery.getResultList();
        Balance startBalance = balanceList.isEmpty() ? null : balanceList.get(0);
        BigDecimal startAmount = startBalance != null ? startBalance.getAmount() : BigDecimal.ZERO;

        String expenseQueryStr = "select sum(o.amount1) from akk_Operation o where o.acc1.id = ?1 and o.opDate <= ?2";
        if (startBalance != null)
            expenseQueryStr += " and o.opDate >= ?3";
        Query expenseQuery = entityManager.createQuery(expenseQueryStr);
        expenseQuery.setParameter(1, accountId);
        expenseQuery.setParameter(2, date);
        if (startBalance != null)
            expenseQuery.setParameter(3, startBalance.getBalanceDate());
        BigDecimal expense = (BigDecimal) expenseQuery.getSingleResult();
        if (expense == null)
            expense = BigDecimal.ZERO;

        String incomeQueryStr = "select sum(o.amount2) from akk_Operation o where o.acc2.id = ?1 and o.opDate <= ?2";
        if (startBalance != null)
            incomeQueryStr += " and o.opDate >= ?3";
        Query incomeQuery = entityManager.createQuery(incomeQueryStr);
        incomeQuery.setParameter(1, accountId);
        incomeQuery.setParameter(2, date);
        if (startBalance != null)
            incomeQuery.setParameter(3, startBalance.getBalanceDate());
        BigDecimal income = (BigDecimal) incomeQuery.getSingleResult();
        if (income == null)
            income = BigDecimal.ZERO;

        return startAmount.add(income).subtract(expense);
    }

    @Transactional
    public void recalculateBalance(UUID accountId) {
        removeBalanceRecords(accountId);

        TreeMap<LocalDate, Balance> balances = new TreeMap<>();

        TypedQuery<Operation> query = entityManager.createQuery("select op from akk_Operation op " +
                "left join op.acc1 a1 left join op.acc2 a2 " +
                "where (a1.id = ?1 or a2.id = ?1) order by op.opDate", Operation.class);
        query.setParameter(1, accountId);
        fetchPlanRepository.getFetchPlan(Operation.class, "operation-recalc-balance");
        List<Operation> operations = query.getResultList();
        for (Operation operation : operations) {
            addOperation(balances, operation, accountId);
        }
        for (Balance balance : balances.values()) {
            entityManager.persist(balance);
        }
    }

    private void removeBalanceRecords(UUID accountId) {
        TypedQuery<Balance> query = entityManager.createQuery("select b from akk_Balance b where b.account.id = ?1", Balance.class);
        query.setParameter(1, accountId);
        List<Balance> list = query.getResultList();
        for (Balance balance : list) {
            entityManager.remove(balance);
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
                balance.setBalanceDate(operation.getOpDate().with(TemporalAdjusters.lastDayOfMonth())/*DateUtils.ceiling(operation.getOpDate(), Calendar.MONTH)*/);
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
