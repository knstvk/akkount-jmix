package akkount.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;

@JmixEntity(name = "akk_CategoryBalance")
public class CategoryAmount extends BaseUuidEntity {

    private Category category;

    private BigDecimal amount;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
