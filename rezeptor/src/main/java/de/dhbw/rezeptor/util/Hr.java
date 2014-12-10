package de.dhbw.rezeptor.util;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class Hr extends Label {

	public Hr() {

                super("<hr/>", Label.CONTENT_XHTML);
	}

	public Hr(String content) {
		super(content);
		// TODO Auto-generated constructor stub
	}

	public Hr(Property contentSource) {
		super(contentSource);
		// TODO Auto-generated constructor stub
	}

	public Hr(String content, ContentMode contentMode) {
		super(content, contentMode);
		// TODO Auto-generated constructor stub
	}

	public Hr(Property contentSource, ContentMode contentMode) {
		super(contentSource, contentMode);
		// TODO Auto-generated constructor stub
	}

}
