package akkount.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum OperationType implements EnumClass<String>{

    EXPENSE("E"),
    INCOME("I"),
    TRANSFER("T");

    private String id;

    OperationType (String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    public static OperationType fromId(String id) {
        for (OperationType at : OperationType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}