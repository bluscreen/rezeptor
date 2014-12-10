package de.dhbw.rezeptor.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import de.dhbw.rezeptor.exceptions.WrongMimeTypeException;

/**
 * Komponente für BildUpload...
 * 
 * @author dhammacher
 * 
 */

public class ImageUploader implements Receiver, SucceededListener {

	private final static Properties prop = SystemProperties.getProperties();

	private Image image;
	private String filename;
	private String filePath;
	private File file;

	public ImageUploader(Image image, String filename, String filepath) {
		super();
		this.image = image;
		this.filename = filename;
		this.filePath = filepath;
	}

	public OutputStream receiveUpload(String oldFileName, String mimeType) {
		// Hier schreiben streamen wir das File rein
		FileOutputStream fos = null;
		// erst mal gucken, ob das auch ein bild ist!
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.addAll(Arrays.asList("image/jpeg", "image/gif", "image/png",
				"image/tiff"));
		try {
			// wenn nicht... dann verschwinde!!
			if (!mimeTypes.contains(mimeType))
				throw new WrongMimeTypeException(mimeType);

			// zur sicherheit mal lieber noch die dateiendung anpassen
			String newFileExt = mimeType.substring(6);

			filename += "_" + new GregorianCalendar().getTimeInMillis()
					+ Generator.randomWord(5, false);

			// Open the file for writing.
			file = new File(filePath + filename + "." + newFileExt);
			fos = new FileOutputStream(file);
		} catch (final java.io.FileNotFoundException e) {
			new Notification(prop.getProperty("error.fileNotFound"),
					e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page
					.getCurrent());
			return null;
		} catch (final WrongMimeTypeException e) {
			new Notification(prop.getProperty("error.wrongMimeType"),
					e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page
					.getCurrent());
			return null;
		}
		return fos; // Return the output stream to write to
	}

	public void uploadSucceeded(SucceededEvent event) {
		// Show the uploaded file in the image viewer
		image.setVisible(true);
		image.setSource(new FileResource(file));
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
