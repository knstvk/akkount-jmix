package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Reports Read", code = "reports-read")
public interface ReportsReadRole {

    @MenuPolicy(menuIds = {"application"})
    void commonMenu();

    @ScreenPolicy(screenIds = {"categories-report", "show-operations"})
    @MenuPolicy(menuIds = {"categories-report"})
    void categoriesReport();
}
