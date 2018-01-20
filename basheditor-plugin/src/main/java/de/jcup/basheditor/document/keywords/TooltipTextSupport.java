package de.jcup.basheditor.document.keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import de.jcup.basheditor.ResourceInputStreamProvider;

public class TooltipTextSupport {
	/**
	 * When a tool tip starts with this identifier this means the tool tip is not plain text but html
	 */
	static final String DIV_HTML_DESCRIPTION = "<div class='htmlDescription'>";
	private static final TooltipTextSupport INSTANCE = new TooltipTextSupport();
	
	public static String getTooltipText(String id){
		return INSTANCE.get(id);
	}
	private Map<String, String> idToTooltipCache = new TreeMap<>();
	private String _tooltip_css;
	private ResourceInputStreamProvider resourceInputStreamProvider;

	TooltipTextSupport(){
		
	}
	
	public static void setTooltipInputStreamProvider(ResourceInputStreamProvider resourceInputStreamProvider) {
		INSTANCE.setResourceInputStreamProvider(resourceInputStreamProvider);
	}
		
	
	public void setResourceInputStreamProvider(ResourceInputStreamProvider resourceInputStreamProvider) {
		this.resourceInputStreamProvider = resourceInputStreamProvider;
	}
	
	public static boolean isHTMLToolTip(String tooltip){
		if (tooltip==null){
			return false;
		}
		return tooltip.startsWith(DIV_HTML_DESCRIPTION);
	}
	
	public static final String getTooltipCSS(){
		return INSTANCE.getCSS();
	}
	
	public String getCSS(){
		if (_tooltip_css!=null){
			return _tooltip_css;
		}
		String path = createPathWithoutFileEnding("_tooltips");
		String loaded = loadFrom(path+".css");
		if (loaded==null){
			_tooltip_css="";
		}else{
			_tooltip_css=loaded;
		}
		return _tooltip_css;
	}
	/**
	 * Resolves tool tip for given id from text files inside tool tip-folder. String result will be cached
	 * @param id
	 * @return tool tip
	 */
	public String get(String id) {
		if (id == null) {
			return "";
		}
		String tooltip = idToTooltipCache.get(id);

		if (tooltip == null) {
			tooltip = load(id);
			idToTooltipCache.put(id, tooltip);
		}
		return tooltip;
	}

	/**
	 * Load tool tip
	 * 
	 * @param id
	 * @return tool tip string - never <code>null</code>
	 */
	String load(String id) {
		if (id==null){
			throw new IllegalArgumentException("id may not be null");
		}
		String path = createPathWithoutFileEnding(id);
		String loaded = loadFrom(path+".html");
		if (loaded!=null){
			return markAsHTMLContent(loaded);
		}
		loaded = loadFrom(path+".txt");
		if (loaded!=null){
			/* text variant is kept as is */
			return loaded;
		}
		return ""; /* fall back */
	}

	protected String createPathWithoutFileEnding(String id) {
		return "/tooltips/" + id;
	}

	private String markAsHTMLContent(String content){
		StringBuilder sb = new StringBuilder();
		
		sb.append(DIV_HTML_DESCRIPTION);
		sb.append(content);
		sb.append("</div>");
		
		return sb.toString();
	}
	
	private String loadFrom(String path) {
	
		try (InputStream inputStream = getInputStream(path)) {
			return loadFromStream(inputStream);
		} catch (IOException e) {
			/* should not happen - but if there are errors
			 * we just return an empty string
			 */
			return null;
		}
	}

	private InputStream getInputStream(String path) throws IOException {
		if (resourceInputStreamProvider==null){
			return null;
		}
		return resourceInputStreamProvider.getStreamFor(path);
	}

	private String loadFromStream(InputStream stream) throws IOException {
		if (stream == null) {
			return null;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}
}
