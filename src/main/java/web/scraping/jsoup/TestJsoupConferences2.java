package web.scraping.jsoup;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TestJsoupConferences2 {
	
	private static final String URL_BASE = "https://investigacion.us.es/sisius/";
	private static final String URL_BASE1 = "https://investigacion.us.es";
	private static final String URL_CONFERENCES = URL_BASE + "sis_showpub.php?idpers=";

	
	public static void main(String[] args) throws MalformedURLException, IOException {

		List<String> researchersLink = new ArrayList<String>();

		Document conferences = Jsoup.connect(URL_BASE).data("text2search", "%%%").data("en", "1").data("inside", "1")
				.maxBodySize(10 * 1024 * 1024).post();
		Elements elements = conferences.select("td.data");
		int index = 0;
		// System.out.println(elements);

		for (Iterator<Element> iterator = elements.iterator(); iterator.hasNext();) {
			index = index + 1;

			Element researcher = iterator.next();

			String link = researcher.getElementsByTag("a").get(0).attr("href");

			if (link.contains("idpers")) {

				researchersLink.add(link);

				// System.out.println(link);
			}

		}

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		int count = 0;
		// int total = 0;

		for (String conferenceID : researchersLink) {
			/*if (count > 100000)
				break;*/
			System.out.println(conferenceID);
			if (conferenceID.substring(0) != "/") {
				conferenceID = "/" + conferenceID;
			}
			
			Document doc = null;
			try {
				doc = Jsoup.parse(new URL(URL_BASE1 + conferenceID), 100000);
			}catch (ConnectException e) {
				System.out.println("Ha petado con la URL:" + conferenceID);
				continue;
			}
			//System.out.println(doc);

			Elements elementos = doc.getElementsByAttributeValue("align", "left");
			Element mainData = elementos.get(0);

			String todosLosElementosEnString = mainData.toString();
			String[] str = todosLosElementosEnString.split("<h5>Aportaciones a Congresos</h5>");
			//String pagina = str[0];
			//System.out.println(pagina);
			if (str.length < 2)
				continue;
			String elementoUno = str[1];
			// System.out.println(elementoUno);
			String[] str1 = elementoUno.split("<h5>");
			String elementoDos = str1[0];
			String[] str2 = elementoDos.split("<h3>");
			String elementoTres = str2[0];
			String[] str3 = elementoTres.split("</div>");
			String elementoCuatro = str3[0];
			// System.out.println(elementoCuatro);

			String conferenceTitle = "";
			String acronym = "";
			String edition = "";
			String city = "";
			String country = "";
			String keywords = "";
			String idConf = "";
			for (String conferenceWithResearchers : elementoCuatro.toString().split("\\n")) {
				if (conferenceWithResearchers.contains("<br>") && conferenceWithResearchers.length() > 10) {
					String[] infoConference = conferenceWithResearchers.split("\\.");
					conferenceTitle = "";
					acronym = "";
					edition = "";
					city = "";
					country = "";
					// Search conference title
					if (infoConference.length >= 3) {
						conferenceTitle = infoConference[2];
						if (conferenceTitle.contains("Comunicación en congreso")
								|| conferenceTitle.contains("Ponencia en jornada")
								|| conferenceTitle.contains("Ponencia en Congreso") 
								|| conferenceTitle.contains("Poster en Congreso")
								|| conferenceTitle.contains("Poster en Jornada")) {
							try {
								conferenceTitle = infoConference[3];
							} catch (IndexOutOfBoundsException e) {
								//System.out.println("La conferencia es:" + infoConference.toString());
							}
						}
						String[] conferenceTitle2 = conferenceTitle.split("[A-Z]+\\s");
						if (conferenceTitle2.length > 1) {
							conferenceTitle = conferenceTitle2[1];
						}
						String[] conferenceTitle3 = conferenceTitle.split("[0-9]+th");
						if (conferenceTitle3.length > 1) {
							conferenceTitle = conferenceTitle3[1];
						}
						String[] conferenceTitle4 = conferenceTitle.split("[0-9]+nd");
						if (conferenceTitle4.length > 1) {
							conferenceTitle = conferenceTitle4[1];
						}
						String[] conferenceTitle5 = conferenceTitle.split("[0-9]+st");
						if (conferenceTitle5.length > 1) {
							conferenceTitle = conferenceTitle5[1];
						}
						String[] conferenceTitle6 = conferenceTitle.split("\\(");
						if (conferenceTitle6.length > 1) {
							conferenceTitle = conferenceTitle6[0];
						}
						String[] conferenceTitle7 = conferenceTitle.split(":");
						if (conferenceTitle7.length > 1) {
							conferenceTitle = conferenceTitle7[0];
						}
						String[] conferenceTitle8 = conferenceTitle.split("[0-9]{4}");
						if (conferenceTitle8.length > 1) {
							conferenceTitle = conferenceTitle8[1];
						}
						String[] conferenceTitle9 = conferenceTitle.split("[0-9]+º");
						if (conferenceTitle9.length > 1) {
							conferenceTitle = conferenceTitle9[1];
						}
						String[] conferenceTitle10 = conferenceTitle.split("[0-9]+rd");
						if (conferenceTitle10.length > 1) {
							conferenceTitle = conferenceTitle10[1];
						}
						String[] conferenceTitle11 = conferenceTitle.split("[0-9]{2}");
						if (conferenceTitle11.length > 1) {
							conferenceTitle = conferenceTitle11[1];
						}
						String[] conferenceTitle12 = conferenceTitle.split("[0-9]+ª");
						if (conferenceTitle12.length > 1) {
							conferenceTitle = conferenceTitle12[1];
						}
						// Search conference title END
					}
					//System.out.println(conferenceTitle);
					
					// Search Acronym
					String firstLetter = "";
					for (int i = 0; i < conferenceTitle.length(); i++) {
						if (Character.isUpperCase(conferenceTitle.charAt(i))) {
							firstLetter = String.valueOf(conferenceTitle.charAt(i));
							acronym = acronym + firstLetter;
						}
					}
					//System.out.println(acronym);
					// Search Acronym END
					
					//Search Edition
					Pattern p = Pattern.compile("\\s[0-9]{4}");
					Matcher m = p.matcher(conferenceWithResearchers);
					
					List<String> possibleEditions = new ArrayList<>();
					while(m.find()) {
						possibleEditions.add(m.group().substring(1, m.group().length()));
					}
					for(String possibleYear : possibleEditions) {
						Integer possibleYearInteger = Integer.valueOf(possibleYear);
						if(possibleYearInteger>1960 && possibleYearInteger <2020)
							edition = possibleYear;
					}
					//System.out.println(edition);
					//Search Edition END
					
					// Search city and country
					
					// Patterns
					Pattern cityAndCountryPattern1 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\s\\(([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\)\\.");
					Pattern cityAndCountryPattern2 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\s\\-\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					Pattern cityAndCountryPattern3 = Pattern.compile("\\.\\s\\,\\s\\-\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					Pattern cityAndCountryPattern4 = Pattern.compile("\\.\\s\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\s\\-\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					//Pattern cityAndCountryPattern5 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\.");
					Pattern cityAndCountryPattern6 = Pattern.compile("\\.\\s\\-\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					Pattern cityAndCountryPattern7 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\.");
					Pattern cityAndCountryPattern8 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					//Santiago de Compostela
					Pattern cityAndCountryPattern9 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\s\\(([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\)\\.");
					//  Sevilla, España, 2014.
					Pattern cityAndCountryPattern10 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([0-9]+)\\.");
					Pattern cityAndCountryPattern11 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\.");
					//Santiago de Compostela, España.
					Pattern cityAndCountryPattern12 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\s\\,([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					//Scottdale, Arizona, EEUU. 
					Pattern cityAndCountryPattern13 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\.");
					//Scottdale, Arizona, Estados Unidos. 
					Pattern cityAndCountryPattern14 = Pattern.compile("\\.\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+)\\,\\s([A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]+\\s*[A-Za-zäÄëËïÏöÖüÜáéíóúáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûàèìòùÀÈÌÒÙÑñ.-]*)\\.");
					// Matchers 
					Matcher cityAndCountryMatcher1 = cityAndCountryPattern1.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher2 = cityAndCountryPattern2.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher3 = cityAndCountryPattern3.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher4 = cityAndCountryPattern4.matcher(conferenceWithResearchers);
					//Matcher cityAndCountryMatcher5 = cityAndCountryPattern5.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher6 = cityAndCountryPattern6.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher7 = cityAndCountryPattern7.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher8 = cityAndCountryPattern8.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher9 = cityAndCountryPattern9.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher10 = cityAndCountryPattern10.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher11 = cityAndCountryPattern11.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher12 = cityAndCountryPattern12.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher13 = cityAndCountryPattern13.matcher(conferenceWithResearchers);
					Matcher cityAndCountryMatcher14 = cityAndCountryPattern14.matcher(conferenceWithResearchers);
					
					if(cityAndCountryMatcher1.find()) {
						//System.out.println("Primer match");
						city = cityAndCountryMatcher1.group(1);
						country = cityAndCountryMatcher1.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher2.find()) {
						//System.out.println("Segundo match");
						city = cityAndCountryMatcher2.group(1);
						country = cityAndCountryMatcher2.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher3.find()) {
						//System.out.println("Tercer match");
						city = cityAndCountryMatcher3.group(1);
						country = cityAndCountryMatcher3.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher4.find()) {
						//System.out.println("Cuarto match");
						city = cityAndCountryMatcher4.group(1);
						country = cityAndCountryMatcher4.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}
					/*else if(cityAndCountryMatcher5.find()) {
						System.out.println("Quinto match");
						city = cityAndCountryMatcher5.group(1);
						//country = cityAndCountryMatcher5.group(2);
						System.out.println("ciudad: " + city + " y pais: " + country);
					}*/else if(cityAndCountryMatcher6.find()) {
						//System.out.println("Sexto match");
						city = cityAndCountryMatcher6.group(1);
						country = cityAndCountryMatcher6.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher7.find()) {
						//System.out.println("Septimo match");
						city = cityAndCountryMatcher7.group(1);
						country = cityAndCountryMatcher7.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher8.find()) {
						//System.out.println("Octavo match");
						city = cityAndCountryMatcher8.group(1);
						country = cityAndCountryMatcher8.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher9.find()) {
						//System.out.println("Noveno match");
						city = cityAndCountryMatcher9.group(1);
						country = cityAndCountryMatcher9.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher10.find()) {
						//System.out.println("Decimo match");
						city = cityAndCountryMatcher10.group(1);
						country = cityAndCountryMatcher10.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher11.find()) {
						//System.out.println("Undecimo match");
						city = cityAndCountryMatcher11.group(1);
						//country = cityAndCountryMatcher11.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher12.find()) {
						//System.out.println("Duodecimo match");
						city = cityAndCountryMatcher12.group(1);
						country = cityAndCountryMatcher12.group(2);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher13.find()) {
						//System.out.println("13 match");
						city = cityAndCountryMatcher13.group(2);
						country = cityAndCountryMatcher13.group(3);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}else if(cityAndCountryMatcher14.find()) {
						//System.out.println("14 match");
						city = cityAndCountryMatcher14.group(2);
						country = cityAndCountryMatcher14.group(3);
						//System.out.println("ciudad: " + city + " y pais: " + country);
					}
					
					/*try {
						city = infoConference[3];
					} catch (IndexOutOfBoundsException e) {
						//System.out.println(infoConference);
					}*/
					
					/*String[] ciudadPaís = city.split(", ");
					if (ciudadPaís.length > 1) {
						city = ciudadPaís[0];
						country = ciudadPaís[ciudadPaís.length - 1];
					}
					String[] ciudadPaís1 = city.split("\\(");
					if (ciudadPaís1.length > 1) {
						city = ciudadPaís1[0];
						country = ciudadPaís1[ciudadPaís1.length - 1];
					}*/
					//System.out.println(city);
					//System.out.println(country);
					
					// Search city and country END
				}
				
				idConf = (acronym + "-" + edition).toLowerCase();
				//System.out.println(idConf);
				
				List<String> l = new ArrayList<String>();
				if (conferenceTitle.length() > 3) {
					if (!map.containsKey(idConf)) {

						l.add(conferenceTitle);
						l.add(acronym);
						l.add(edition);
						l.add(city);
						l.add(country);
						keywords = acronym;
						l.add(keywords);
						map.put(idConf, l);
					}
				}
				count++;
			}
			// var idConference = newConference.acronym.concat("-".concat(newConference.edition));
            // newConference.idConference = idConference.toLowerCase();
			//System.out.println(map.keySet());
			//System.out.println(map.values());
		}
		
		//MongoClientURI uri = new MongoClientURI("mongodb://jihaneF:jihaneF@ds151355.mlab.com:51355/si1718-jf-conferences");
		MongoClientURI uri = new MongoClientURI("mongodb://jiji:jiji@ds141766.mlab.com:41766/si1718-jf-conferences2");
		MongoClient client = new MongoClient(uri);
		MongoDatabase db = client.getDatabase(uri.getDatabase());
		MongoCollection<org.bson.Document> docResearchers = db.getCollection("conferences");
		
		
		List<org.bson.Document> listaGenerada = new ArrayList<>();
 		for (String conferenceId: map.keySet()) {
			List<String> conference = map.get(conferenceId);
			org.bson.Document document = new org.bson.Document();
			
			document.append("idConference", conferenceId);
			document.append("conference", conference.get(0));
			document.append("acronym", conference.get(1));
			document.append("edition", conference.get(2));
			document.append("city", conference.get(3));
			document.append("country", conference.get(4));
			document.append("keywords", conference.get(5));
			

			//docResearchers.insertOne(documentDetail);
			listaGenerada.add(document);
		}
		docResearchers.insertMany(listaGenerada);
	}
}
