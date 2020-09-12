package akkount.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;

import java.math.BigDecimal;

@ModelObject(name = "akk_CategoryBalance")
public class CategoryAmount extends BaseUuidEntity {

    @ModelProperty
    private Category category;

    @ModelProperty
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
