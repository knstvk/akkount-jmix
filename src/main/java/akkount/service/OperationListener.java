package akkount.service;

import akkount.entity.Account;
import akkount.entity.Balance;
import akkount.entity.Operation;
import akkount.event.BalanceChangedEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.flowui.UiEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static io.jmix.core.event.EntityChangedEvent.Type.DELETED;
import static io.jmix.core.event.EntityChangedEvent.Type.UPDATED;
import static java.util.Objects.requireNonNull;

@Component
public class OperationListener {

    private static final Logger log = LoggerFactory.getLogger(OperationListener.class);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private UiEventPublisher uiEventPublisher;

    private volatile boolean balanceChangedEventsEnabled = true;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOperationChanged(EntityChangedEvent<Operation> event) {
        log.debug("onOperationChanged: event={}", event);

        AttributeChanges changes = event.getChanges();
        LocalDate opDate = changes.getOldValue("opDate");
        BigDecimal amount1 = changes.getOldValue("amount1");
        BigDecimal amount2 = changes.getOldValue("amount2");
        if (event.getType() == DELETED) {
            removeOperation(
                    requireNonNull(opDate),
                    changes.getOldReferenceId("acc1"),
                    changes.getOldReferenceId("acc2"),
                    amount1,
                    amount2
            );
        } else {
            Operation operation = dataManager.load(event.getEntityId()).one();
            if (event.getType() == UPDATED) {
                removeOperation(
                        changes.isChanged("opDate") ? requireNonNull(opDate) : operation.getOpDate(),
                        changes.isChanged("acc1") ? changes.getOldReferenceId("acc1") : idOfNullable(operation.getAcc1()),
                        changes.isChanged("acc2") ? changes.getOldReferenceId("acc2") : idOfNullable(operation.getAcc2()),
                        changes.isChanged("amount1") ? amount1 : operation.getAmount1(),
                        changes.isChanged("amount2") ? amount2 : operation.getAmount2()
                );
            }
            addOperation(operation);
            saveUserData(operation);
        }
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOperationChangedAndCommitted(EntityChangedEvent<Operation> event) {
        if (balanceChangedEventsEnabled) {
            uiEventPublisher.publishEvent(new BalanceChangedEvent(this));
        }
    }

    public void enableBalanceChangedEvents(boolean enable) {
        balanceChangedEventsEnabled = enable;
    }

    private void removeOperation(LocalDate opDate, @Nullable Id<Account> acc1Id, @Nullable Id<Account> acc2Id,
                                 @Nullable BigDecimal amount1, @Nullable BigDecimal amount2) {
        log.debug("removeOperation: opDate={}, acc1Id={}, acc2Id={}, amount1={}, amount2={}", opDate, acc1Id, acc2Id, amount1, amount2);

        if (acc1Id != null) {
            List<Balance> list = getBalanceRecords(opDate, acc1Id);
            if (!list.isEmpty()) {
                for (Balance balance : list) {
                    balance.setAmount(balance.getAmount().add(amount1));
                    dataManager.save(balance);
                }
            }
        }

        if (acc2Id != null) {
            List<Balance> list = getBalanceRecords(opDate, acc2Id);
            if (!list.isEmpty()) {
                for (Balance balance : list) {
                    balance.setAmount(balance.getAmount().subtract(amount2));
                    dataManager.save(balance);
                }
            }
        }
    }

    private void addOperation(Operation operation) {
        log.debug("addOperation: {}", operation);

        if (operation.getAcc1() != null) {
            List<Balance> list = getBalanceRecords(operation.getOpDate(), Id.of(operation.getAcc1()));
            if (list.isEmpty()) {
                Balance balance = dataManager.create(Balance.class);
                balance.setAccount(operation.getAcc1());
                balance.setAmount(operation.getAmount1().negate()
                        .add(previousBalanceAmount(operation.getAcc1(), operation.getOpDate())));
                balance.setBalanceDate(nextBalanceDate(operation.getOpDate()));
                dataManager.save(balance);
            } else {
                for (Balance balance : list) {
                    balance.setAmount(balance.getAmount().subtract(operation.getAmount1()));
                    dataManager.save(balance);
                }
            }
        }

        if (operation.getAcc2() != null) {
            List<Balance> list = getBalanceRecords(operation.getOpDate(), Id.of(operation.getAcc2()));
            if (list.isEmpty()) {
                Balance balance = dataManager.create(Balance.class);
                balance.setAccount(operation.getAcc2());
                balance.setAmount(operation.getAmount2()
                        .add(previousBalanceAmount(operation.getAcc2(), operation.getOpDate())));
                balance.setBalanceDate(nextBalanceDate(operation.getOpDate()));
                dataManager.save(balance);
            } else {
                for (Balance balance : list) {
                    balance.setAmount(balance.getAmount().add(operation.getAmount2()));
                    dataManager.save(balance);
                }
            }
        }
    }

    private List<Balance> getBalanceRecords(LocalDate opDate, Id<Account> accId) {
        log.debug("getBalanceRecords: opDate={}, accId={}", opDate, accId);

        return dataManager.load(Balance.class)
                .query("select b from akk_Balance b " +
                        "where b.account.id = :accountId and b.balanceDate > :balanceDate order by b.balanceDate")
                .parameter("accountId", accId.getValue())
                .parameter("balanceDate", opDate)
                .list();
    }

    private BigDecimal previousBalanceAmount(Account account, LocalDate opDate) {
        log.debug("previousBalanceAmount: acccount={}, opDate={}", account, opDate);

        Optional<Balance> optBalance = dataManager.load(Balance.class)
                .query("select b from akk_Balance b " +
                        "where b.account.id = :accountId and b.balanceDate <= :balanceDate order by b.balanceDate desc")
                .parameter("accountId", account.getId())
                .parameter("balanceDate", opDate)
                .maxResults(1)
                .optional();
        return optBalance.map(Balance::getAmount).orElse(BigDecimal.ZERO);
    }

    private LocalDate nextBalanceDate(LocalDate opDate) {
        return opDate.with(TemporalAdjusters.firstDayOfNextMonth());
    }

    private void saveUserData(Operation operation) {
        switch (operation.getOpType()) {
            case EXPENSE:
                userDataService.saveEntity(UserDataKeys.OP_EXPENSE_ACCOUNT, operation.getAcc1(), false);
                break;
            case INCOME:
                userDataService.saveEntity(UserDataKeys.OP_INCOME_ACCOUNT, operation.getAcc2(), false);
                break;
            case TRANSFER:
                userDataService.saveEntity(UserDataKeys.OP_TRANSFER_EXPENSE_ACCOUNT, operation.getAcc1(), false);
                userDataService.saveEntity(UserDataKeys.OP_TRANSFER_INCOME_ACCOUNT, operation.getAcc2(), false);
                break;
        }
    }

    @Nullable
    private <T> Id<T> idOfNullable(@Nullable T entity) {
        return entity == null ? null : Id.of(entity);
    }
}
