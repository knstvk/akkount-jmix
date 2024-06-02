package akkount.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JmixEntity
@Table(name = "AKK_CATEGORY_GUESS_RULE", indexes = {
        @Index(name = "IDX_CATEGORY_GUESS_RULE_ACCOUNT", columnList = "ACCOUNT_ID"),
        @Index(name = "IDX_CATEGORY_GUESS_RULE_CATEGORY", columnList = "CATEGORY_ID")
})
@Entity
public class CategoryGuessRule {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    @Column(name = "CAT_TYPE", nullable = false, length = 1)
    @NotNull
    private String catType;

    @JoinColumn(name = "ACCOUNT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Comment("Groovy expression: \"amount\" variable, boolean result")
    @Column(name = "EXPRESSION", nullable = false)
    @Lob
    @NotNull
    private String expression;

    @Comment("Smaller number gives higher priority")
    @Column(name = "PRIORITY", nullable = false)
    @NotNull
    private Integer priority;

    public CategoryType getCatType() {
        return catType == null ? null : CategoryType.fromId(catType);
    }

    public void setCatType(CategoryType catType) {
        this.catType = catType == null ? null : catType.getId();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}