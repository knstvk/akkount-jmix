package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Kids", code = "kids")
public interface KidsRole extends
        UiMinimalRole,
        MasterDataReadRole, OperationsFullAccessRole {

    @ViewPolicy(viewIds = "akk_PreferencesScreen")
    @MenuPolicy(menuIds = {"akk_PreferencesScreen"})
    void screens();
}
