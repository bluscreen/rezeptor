package de.dhbw.rezeptor;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
/**
 * Diese UI Klasse wird an mobile endgeräte ausgegben...
 * erst mal nicht so wichtig..
 * @author dhammacher
 *
 */
@Theme(Reindeer.THEME_NAME)
@Title("Rezeptor")
public class MobileCheckUI extends UI {

    @Override
    protected void init(final VaadinRequest request) {
        setWidth("400px");
        setContent(new VerticalLayout() {
            {
                setMargin(true);
                addComponent(new Label(
                        "<h1>Geheimrezeptor Dashboard</h1><h3>This Vaadin application is not designed for mobile devices.</h3><p>If you wish, you can continue to <a href=\""
                                + request.getContextPath()
                                + request.getPathInfo()
                                + "?mobile=false\">load it anyway</a>.</p>",
                        ContentMode.HTML));
            }
        });

    }
}
