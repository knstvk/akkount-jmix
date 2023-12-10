package akkount.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity
@Table(name = "AKK_BALANCE")
@Entity(name = "akk_Balance")
public class Balance extends StandardEntity {

    @Column(name = "BALANCE_DATE", nullable = false)
    protected LocalDate balanceDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    protected Account account;

    @Column(name = "AMOUNT", precision = 19, scale = 2)
    protected BigDecimal amount = BigDecimal.ZERO;

    public void setBalanceDate(LocalDate balanceDate) {
        this.balanceDate = balanceDate;
    }

    public LocalDate getBalanceDate() {
        return balanceDate;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }


}