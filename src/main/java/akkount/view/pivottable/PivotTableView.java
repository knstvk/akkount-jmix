package akkount.view.pivottable;


import akkount.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "pivot-table", layout = MainView.class)
@ViewController(id = "PivotTableView")
@ViewDescriptor(path = "pivot-table-view.xml")
public class PivotTableView extends StandardView {
}