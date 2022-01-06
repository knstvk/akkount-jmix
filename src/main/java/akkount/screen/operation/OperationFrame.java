package akkount.screen.operation;

import akkount.entity.Operation;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.ValidationErrors;

public interface OperationFrame {

    Fragment getFragment();

    void postInit(Operation item);

    void postValidate(ValidationErrors errors);
}
