package akkount.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@JmixEntity
@Table(name = "AKK_BALANCE")
@Entity(name = "akk_Balance")
public class Balance extends StandardEntity {

    @Temporal(TemporalType.DATE)
    @Column(name = "BALANCE_DATE", nullable = false)
    protected Date balanceDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    protected Account account;

    @Column(name = "AMOUNT", precision = 19, scale = 2)
    protected BigDecimal amount = BigDecimal.ZERO;

    public void setBalanceDate(Date balanceDate) {
        this.balanceDate = balanceDate;
    }

    public Date getBalanceDate() {
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