package akkount.security;

import akkount.entity.Operation;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "Kids restrictions", code = "kids-restrictions")
public interface KidsRowLevelRole {

    @JpqlRowLevelPolicy(entityClass = Operation.class, where = "{E}.createdBy = :current_user_username")
    void operations();
}
