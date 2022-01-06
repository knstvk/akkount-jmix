package akkount.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JmixEntity
@Table(name = "AKK_CATEGORY")
@Entity(name = "akk_Category")
public class Category extends StandardEntity {

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    protected String name;

    @Column(name = "DESCRIPTION", length = 100)
    protected String description;

    @Column(name = "CAT_TYPE", nullable = false)
    protected String catType;

    private static final long serialVersionUID = 5768212165913475729L;

    public void setCatType(CategoryType catType) {
        this.catType = catType == null ? null : catType.getId();
    }

    public CategoryType getCatType() {
        return catType == null ? null : CategoryType.fromId(catType);
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}