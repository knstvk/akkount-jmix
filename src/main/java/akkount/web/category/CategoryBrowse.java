package akkount.web.category;

import akkount.entity.Category;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("akk_Category.lookup")
@UiDescriptor("category-browse.xml")
@LookupComponent("categoryTable")
public class CategoryBrowse extends StandardLookup<Category> {
}