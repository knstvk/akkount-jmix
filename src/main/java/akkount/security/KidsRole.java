package akkount.security;

import akkount.entity.Operation;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.Role;

@Role(name = "Kids", code = "kids")
public interface KidsRole extends MasterDataReadRole, OperationsFullAccessRole {

    @JpqlRowLevelPolicy(entityClass = Operation.class, where = "{E}.createdBy = :session$username")
    void operations();
}
