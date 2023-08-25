package akkount.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Table(name = "AKK_CURRENCY")
@Entity(name = "akk_Currency")
public class Currency extends StandardEntity {

    @InstanceName
    @Length(max = 3)
    @Column(name = "CODE", nullable = false, length = 3)
    protected String code;

    @Column(name = "NAME", length = 50)
    protected String name;

    private static final long serialVersionUID = -3270758636888264613L;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}