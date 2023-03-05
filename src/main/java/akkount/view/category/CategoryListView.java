package akkount.view.category;

import akkount.entity.Category;

import akkount.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "categories", layout = MainView.class)
@ViewController("akk_Category.list")
@ViewDescriptor("category-list-view.xml")
@LookupComponent("categoriesTable")
@DialogMode(width = "50em", height = "37.5em")
public class CategoryListView extends StandardListView<Category> {
}