package de.dhbw.rezeptor;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import de.dhbw.rezeptor.view.View_Rezeptor;
/**
 * Hilfe Manager... irgendwie will der gerad nich so recht... checken.
 * @author dhammacher
 *
 */
public class HelpManager {

    private UI ui;
    private List<HelpOverlay> overlays = new ArrayList<HelpOverlay>();

    public HelpManager(UI ui) {
        this.ui = ui;
    }

    public void closeAll() {
        for (HelpOverlay overlay : overlays) {
            overlay.close();
        }
        overlays.clear();
    }

    public void showHelpFor(View view) {
         showHelpFor(view.getClass());
    }

    public void showHelpFor(Class<? extends View> view) {
		if (view == View_Rezeptor.class) {
			addOverlay(
					"Add new data sets",
					"You can add new data sets to the graph by choosing a title from the combo box and clicking \"Add\".",
					"timeline-add");
		} else if (view == null) {
			
			addOverlay("Clear graph", "Clear all data sets from the graph",
					"timeline-clear");
			addOverlay(
					"Browse",
					"The Timeline component allows you to browse through and zoom the data sets infinitely.",
					"timeline-browse").center();
			addOverlay(
					"Unlimited Data",
					"You can scroll through any number of rows in the table with blazing speed",
					"table-lazy").center();
			addOverlay(
					"Filter",
					"Live filter the table contents (in this demo you can only filter the country field",
					"table-filter");
			addOverlay(
					"Create a report",
					"You can select some rows from the table, and then either drag them over to the \"Reports\" tab in the sidebar or click this button or open the context menu for the items and select \"Create Report\"",
					"table-rows");
		} 
    }

    public HelpOverlay addOverlay(String caption, String text, String style) {
        HelpOverlay o = new HelpOverlay();
        o.setCaption(caption);
        o.addComponent(new Label(text, ContentMode.HTML));
        o.setStyleName(style);
        // ui.addWindow(o);
        overlays.add(o);
        return o;
    }

}
