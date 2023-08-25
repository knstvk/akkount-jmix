package akkount.security;

import akkount.entity.CategoryAmount;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Parents", code = "parents")
public interface ParentsRole extends
        UiMinimalRole,
        MasterDataFullAccessRole, OperationsFullAccessRole, ReportsReadRole {

    @SpecificPolicy(resources = "get-balance")
    void balance();

    @ViewPolicy(viewIds = {"akk_PreferencesScreen", "ShowOperations"})
    @MenuPolicy(menuIds = "akk_PreferencesScreen")
    void screens();


    @EntityPolicy(entityClass = CategoryAmount.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = CategoryAmount.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void categoryReport();
}
