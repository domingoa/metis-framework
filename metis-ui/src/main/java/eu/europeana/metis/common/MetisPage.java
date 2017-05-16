package eu.europeana.metis.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import eu.europeana.metis.mapping.organisms.global.Footer;
import eu.europeana.metis.mapping.organisms.global.NavigationTop;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;

/**
 * This is common Metis page with the same assets, bread-crumbs and header instantiated.
 * @author alena
 *
 */
public abstract class MetisPage extends AbstractMetisPage {
	
	public abstract Byte resolveCurrentPage();
	
	@Override
	public List<Entry<String, String>> resolveCssFiles() {
		return Arrays.asList(new SimpleEntry<String, String>("https://europeana-styleguide-test.s3.amazonaws.com/css/pandora/screen.css", "all"),
//		return Arrays.asList(new SimpleEntry<String, String>("http://localhost/css/pandora/screen.css", "all"),
				new SimpleEntry<String, String>("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css", "all"));
	}

	@Override
	public List<Entry<String, String>> resolveJsFiles() {
		return Arrays.asList(new SimpleEntry<String, String>("https://europeana-styleguide-test.s3.amazonaws.com/js/modules/require.js", 
				"https://europeana-styleguide-test.s3.amazonaws.com/js/modules/main/templates/main-pandora"));
		//to test the java app with a local styleguide assets!
//		return Arrays.asList(new SimpleEntry<String, String>("http://localhost/js/modules/require.js", 
//				"http://localhost/js/modules/main/templates/main-pandora"));
	}

	@Override
	public List<Entry<String, String>> resolveJsVars() {
		return Arrays.asList(new SimpleEntry<String, String>("pageName", "portal/index"));
	}

	@Override
	public List<Entry<String, String>> resolveBreadcrumbs() {
		List<Entry<String, String>> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(new SimpleEntry<String, String>("Home", "/"));
		return breadcrumbs;
	}

	@Override
	public NavigationTop buildHeader() {
		//commented for the new design!
		NavigationTop header = new NavigationTop("#", "Home", true);
		header.addNextPrev("next_url_here", "prev_url_here", "results_url_here");
		header.addGlobal(false, true, true, "#", "Europeana Metis", "main-menu", null, buildUtilityNavigation());		
		return header;
	}
	
	@Override
	public Footer buildFooter() {
		List<Entry<String, String>> linkList1 = Arrays.asList(
				new AbstractMap.SimpleEntry<String, String>("About", "/"),
				new AbstractMap.SimpleEntry<String, String>("Development updates", "/"),
				new AbstractMap.SimpleEntry<String, String>("All institutions", "/"),
				new AbstractMap.SimpleEntry<String, String>("Become our partner", "/"),
				new AbstractMap.SimpleEntry<String, String>("Contact us", "/")); 
		String linkListTitle1 = "More Info"; 
		
		List<Entry<String, String>> linkList2 = Arrays.asList(
				new AbstractMap.SimpleEntry<String, String>("Search tips", "/"),
				new AbstractMap.SimpleEntry<String, String>("Terms of Use & Policies", "/"));  
		String linkListTitle2 = "Help";
		
		List<Entry<String, String>> linkList3 = Arrays.asList(
				new AbstractMap.SimpleEntry<String, String>("API Docs", "/"),
				new AbstractMap.SimpleEntry<String, String>("Status", "/")); 
		String linkListTitle3 = "Tools"; 
		
		List<Entry<String, String>> subFooter = Arrays.asList(
				new AbstractMap.SimpleEntry<String, String>("Home", "/"),
				new AbstractMap.SimpleEntry<String, String>("Terms of use and policies", "http://europeana.eu/portal/rights/terms-and-policies.html"),
				new AbstractMap.SimpleEntry<String, String>("Contact us", "/"),
				new AbstractMap.SimpleEntry<String, String>("Sitemap", "/"));
		
		Map<String, Boolean> social = new HashMap<>();
		social.put("linkedin", false);
		social.put("github", false);
		social.put("twitter", true);
		social.put("facebook", true);
		social.put("pinterest", true);
		social.put("googleplus", true);
		
		return new Footer(linkList1, linkListTitle1, 
						linkList2, linkListTitle2, 
						linkList3, linkListTitle3, 
						subFooter, social);
	}
	
	/**
	 * Welcome message for a logged in user
	 * @param user_name
	 * @return
	 */
	// TODO move this method to Metis Dashboard page implementation when it is ready.
	protected Map<String, String> createWelcomeMessage(String user_name) {
		Map<String, String> welcome_message = new HashMap<>();
		welcome_message.put("text_first", text_first);
		welcome_message.put("user_name", user_name != null && !user_name.isEmpty() ? user_name : "User");
		welcome_message.put("text_end", text_end);
		return welcome_message;
	}
	
	/**
	 * The method builds the model for the top-right menu Settings on a Metis page.
	 * @return the list utility navigation menu items.
	 */
	public abstract List<NavigationTopMenu> buildUtilityNavigation();
}