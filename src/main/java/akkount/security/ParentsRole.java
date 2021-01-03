package akkount.security;

import io.jmix.security.role.annotation.Role;
import io.jmix.security.role.annotation.SpecificPolicy;

@Role(name = "Parents", code = "parents")
public interface ParentsRole extends MasterDataFullAccessRole, OperationsFullAccessRole, ReportsReadRole {

    @SpecificPolicy(resources = {"get-balance"})
    void balance();
}
