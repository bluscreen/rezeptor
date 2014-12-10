package de.dhbw.rezeptor.data;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;

/**
 * wurde zum zwecke der Conversion von Domain Objekten bedacht. Durch eine
 * Konsequente toString() verwendung, wurde diese komponente nicht nötig
 * 
 * @author dhammacher
 * 
 */
@SuppressWarnings("serial")
public class MyConverterFactory extends DefaultConverterFactory {
	@SuppressWarnings("unchecked")
	@Override
	protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> findConverter(
			Class<PRESENTATION> presentationType, Class<MODEL> modelType) {

		// if (presentationType == String.class
		// && modelType == Mengeneinheit.class) {
		// return ((Converter<PRESENTATION, MODEL>) new Converter<String,
		// Mengeneinheit>() {
		//
		// @Override
		// public Class<Mengeneinheit> getModelType() {
		// return Mengeneinheit.class;
		// }
		//
		// @Override
		// public Class<String> getPresentationType() {
		// return String.class;
		// }
		//
		// @Override
		// public Mengeneinheit convertToModel(String value,
		// Class<? extends Mengeneinheit> targetType, Locale locale)
		// throws com.vaadin.data.util.converter.Converter.ConversionException {
		// Mengeneinheit m = new Mengeneinheit();
		// m.setBezeichnung(value);
		// return m;
		// }
		//
		// @Override
		// public String convertToPresentation(Mengeneinheit value,
		// Class<? extends String> targetType, Locale locale)
		// throws com.vaadin.data.util.converter.Converter.ConversionException {
		// return value.getBezeichnung();
		// }
		//
		// });
		// }
		// else if (presentationType == String.class
		// && modelType == Zutat.class) {
		// return ((Converter<PRESENTATION, MODEL>) new Converter<String,
		// Zutat>() {
		//
		// @Override
		// public Class<Zutat> getModelType() {
		// return Zutat.class;
		// }
		//
		// @Override
		// public Class<String> getPresentationType() {
		// return String.class;
		// }
		//
		// @Override
		// public Zutat convertToModel(String value,
		// Class<? extends Zutat> targetType, Locale locale)
		// throws com.vaadin.data.util.converter.Converter.ConversionException {
		// Zutat m = new Zutat();
		// m.setBezeichnung(value);
		// return m;
		// }
		//
		// @Override
		// public String convertToPresentation(Zutat value,
		// Class<? extends String> targetType, Locale locale)
		// throws com.vaadin.data.util.converter.Converter.ConversionException {
		// return value.getBezeichnung();
		// }
		//
		// });
		// }

		return super.findConverter(presentationType, modelType);
	}
}
