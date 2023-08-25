package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Reports Read", code = "reports-read")
public interface ReportsReadRole {

    @MenuPolicy(menuIds = "application")
    void commonMenu();

    @ViewPolicy(viewIds = {"categories-report", "ShowOperations"})
    @MenuPolicy(menuIds = {"categories-report"})
    void categoriesReport();
}
