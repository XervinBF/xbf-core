package org.xbf.core.Messages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Styled Response
 * @author BL19
 * @see org.xbf.core.Messages.Response
 */
public class RichResponse {

	public ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
	public String title;
	public ArrayList<Pair<String, String>> commands = new ArrayList<Pair<String, String>>();
	public Color color;
	public String footer;
	public String description;
	
	/**
	 * Creates a new {@link org.xbf.core.Messages.RichResponse RichResponse} with a Title. For no title use an empty string or null.
	 * @param title The title to use, or empty / null for no title.
	 */
	public RichResponse(String title) {
		this.title = title;
		if(this.title != null && this.title.trim().equals("")) this.title = null;
	}
	
	/**
	 * Adds a field to the current {@link org.xbf.core.Messages.RichResponse RichResponse}
	 * @param title The Title of the field
	 * @param content The content of the Field
	 * @return The current instance of {@link org.xbf.core.Messages.RichResponse RichResponse}
	 */
	public RichResponse addField(String title, String content) {
		fields.add(new Pair<String, String>(title, content));
		return this;
	}
	
	/**
	 * Adds a command to the current {@link org.xbf.core.Messages.RichResponse RichResponse}
	 * @param title The display title of the command
	 * @param command The command to run when clicked
	 * @return The current instance of {@link org.xbf.core.Messages.RichResponse RichResponse}
	 */
	public RichResponse addCommand(String title, String command) {
		commands.add(new Pair<String, String>(title, command));
		return this;
	}
	
	/**
	 * Sets the color of the current {@link org.xbf.core.Messages.RichResponse RichResponse}
	 * @param color The color to use
	 * @return The current instance of {@link org.xbf.core.Messages.RichResponse RichResponse}
	 */
	public RichResponse setColor(Color color) {
		this.color = color;
		return this;
	}
	
	/**
	 * Sets the footer text
	 * @param footer The footer to use
	 * @return The current instance of {@link org.xbf.core.Messages.RichResponse RichResponse}
	 */
	public RichResponse setFooter(String footer) {
		this.footer = footer;
		return this;
	}
	
	/**
	 * Sets the description text
	 * @param description The description text to use
	 * @return The current instance of {@link org.xbf.core.Messages.RichResponse RichResponse}
	 */
	public RichResponse setDescription(String description) {
		this.description = description;
		return this;
	}
	
}
