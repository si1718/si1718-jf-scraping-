package web.scraping.jsoup;

import java.io.IOException;

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

public class TestJsoupConferences {

	private static final String URL_BASE = "https://investigacion.us.es/sisius/";
	private static final String URL_SCHOLAR = URL_BASE + "sis_showpub.php?idpers=";
	private static final String URL_BASE1 = "https://investigacion.us.es";

	public static void main(String[] args) throws MalformedURLException, IOException {

		Document doc = Jsoup.connect(URL_BASE).data("text2search", "%%%").data("en", "1").data("inside", "1")
				.maxBodySize(10 * 1024 * 1024).post();
		Elements elements = doc.select("td.data");
		int index = 0;
		List<String> researchersLink = new ArrayList<String>();
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
		
		for (String str : researchersLink) {
			if(count>100) break;
			System.out.println(str);
			if (str.substring(0) != "/") {
				str = "/" + str;
			}
			Document doc1 = Jsoup.parse(new URL(URL_BASE1 + str), 10000);
			// System.out.println(doc1);
			Elements elementos = doc1.getElementsByAttributeValue("align", "left");
			Element mainData = elementos.get(0);

			String todosLosElementosEnString = mainData.toString();
			String[] str0 = todosLosElementosEnString.split("<h5>Aportaciones a Congresos</h5>");
			// System.out.println(str0);
			if (str0.length < 2)
				continue;
			String elem1 = str0[1];
			// System.out.println(elem1);
			String[] str1 = elem1.split("<h5>");
			String elem2 = str1[0];

			
			String temp = "";
			String edition = "";
			String acronym = "";
			String city = "";
			String country = "";
			String keywords = "";
			for (String s : elem2.toString().split("\\n")) {
				if (s.contains("<br>") && s.length() > 10) {
					String[] aux = s.split("\\.");
					temp = "";
					edition = "";
					acronym = "";
					
					// Search edition
					Pattern p = Pattern.compile("\\s[0-9]{4}");
					Matcher m = p.matcher(s);
					
					List<String> possibleEditions = new ArrayList<>();
					while(m.find()) {
						possibleEditions.add(m.group().substring(1, m.group().length()));
						//edition = m.group().substring(1, m.group().length());
					}
					for(String possibleYear : possibleEditions) {
						Integer possibleYearInteger = Integer.valueOf(possibleYear);
						if(possibleYearInteger>1960 && possibleYearInteger <2020)
							edition = possibleYear;
					}
					
					// Search edition END
					
					// Search conference title
					
					if (aux.length >= 3) {
						temp = aux[2];
						if(temp.contains("Comunicación en congreso") || temp.contains("Ponencia en jornada")|| temp.contains("Ponencia en Congreso"))
							temp = aux[3];
						String[] temp2 = temp.split("[A-Z]+\\s");
						if (temp2.length > 1) {
							temp = temp2[1];
						}
						String[] temp3 = temp.split("[0-9]+th");
						if (temp3.length > 1) {
							temp = temp3[1];
						}
						String[] temp4 = temp.split("[0-9]+nd");
						if (temp4.length > 1) {
							temp = temp4[1];
						}
						String[] temp5 = temp.split("[0-9]+st");
						if (temp5.length > 1) {
							temp = temp5[1];
						}
						String[] temp6 = temp.split("\\(");
						if (temp6.length > 1) {
							temp = temp6[0];
						}
						String[] temp7 = temp.split(":");
						if (temp7.length > 1) {
							temp = temp7[0];
						}
						String[] temp10 = temp.split("[0-9]{4}");
						if(temp10.length > 1) {
							temp = temp10[1];
						}
						/*try {
							 edition = aux[4].substring(1);
						} catch (IndexOutOfBoundsException e) {

							// System.out.println("El array es: " + aux.toString());
						}*/

						//System.out.println(temp);
						// System.out.println(edition);
						
						// Search conference title END
						
						// Search city and country
						
						// Patterns
						Pattern cityAndCountryPattern1 = Pattern.compile("\\.\\s([A-Za-z]+\\s*[A-Za-z]*)\\s\\(([A-Za-z]+)\\)\\.");
						Pattern cityAndCountryPattern2 = Pattern.compile("\\.\\s([A-Za-z]+\\s*[A-Za-z]*)\\s\\-\\s([A-Za-z]+)\\.");
						Pattern cityAndCountryPattern3 = Pattern.compile("\\.\\s\\,\\s\\-\\s([A-Za-z]+\\s*[A-Za-z]*)\\,\\s([A-Za-z]+)\\.");
						Pattern cityAndCountryPattern4 = Pattern.compile("\\.\\s\\s([A-Za-z]+\\s*[A-Za-z]*)\\s\\-\\s([A-Za-z]+)\\.");
						Pattern cityAndCountryPattern5 = Pattern.compile("\\.\\s([A-Za-z]+\\s*[A-Za-z]*)\\.");
						//. , - Lisboa, Portugal
						// Matchers 
						Matcher cityAndCountryMatcher1 = cityAndCountryPattern1.matcher(s);
						Matcher cityAndCountryMatcher2 = cityAndCountryPattern2.matcher(s);
						Matcher cityAndCountryMatcher3 = cityAndCountryPattern3.matcher(s);
						Matcher cityAndCountryMatcher4 = cityAndCountryPattern4.matcher(s);
						Matcher cityAndCountryMatcher5 = cityAndCountryPattern5.matcher(s);


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
						}else if(cityAndCountryMatcher5.find()) {
							//System.out.println("Quinta match");
							city = cityAndCountryMatcher5.group(1);
							//System.out.println("ciudad: " + city + " y pais: " + country);
						}
						
						try {
							city = aux[3];
						} catch (IndexOutOfBoundsException e) {
							//System.out.println(aux);
						}
						
						
						
						String[] temp8 = city.split(", ");
						if (temp8.length > 1) {
							city = temp8[0];
							country = temp8[temp8.length - 1];
						}
						String[] temp9 = city.split("\\(");
						if (temp9.length > 1) {
							city = temp9[0];
							country = temp9[temp9.length - 1];
						}
						// System.out.println(city);
						// System.out.println(country);
						
						// Search city and country END
					}
					
					//Search Acronym
					String firstLetter = "";
					// String edition = "";
					for (int i = 0; i < temp.length(); i++) {
						if (Character.isUpperCase(temp.charAt(i))) {
							firstLetter = String.valueOf(temp.charAt(i));
							acronym = acronym + firstLetter;
							// edition = s.substring(s.length()-1);

						}
					}
					System.out.println(acronym);
					
					// Search Acronym END
				}
				
				List<String> l = new ArrayList<String>();
				if (temp.length() > 3) {
					if (!map.containsKey(acronym + "-" + edition)) {

						l.add(temp);
						l.add(acronym);
						l.add(edition);
						l.add(city);
						l.add(country);
						keywords = edition + "," + city + "," + country;
						l.add(keywords);
						map.put(acronym + "-" + edition, l);
						// System.out.println(temp);
					}
				}
				count++;
			}
			//System.out.println(map.keySet());
			//System.out.println(map.values());
			
		}
		
		//MongoClientURI uri = new MongoClientURI("mongodb://jihaneF:jihaneF@ds151355.mlab.com:51355/si1718-jf-conferences");
		MongoClientURI uri = new MongoClientURI("mongodb://jiji:jiji@ds141766.mlab.com:41766/si1718-jf-conferences2");
		MongoClient client = new MongoClient(uri);
		MongoDatabase db = client.getDatabase(uri.getDatabase());
		MongoCollection<org.bson.Document> docResearchers = db.getCollection("conferences");
		
		for (String conferenceId: map.keySet()) {
			List<String> conference = map.get(conferenceId);
			BasicDBObject document = new BasicDBObject();
			
			org.bson.Document documentDetail = new org.bson.Document();
			documentDetail.put("idConference", conferenceId);
			documentDetail.put("conference", conference.get(0));
			documentDetail.put("acronym", conference.get(1));
			documentDetail.put("edition", conference.get(2));
			documentDetail.put("city", conference.get(3));
			documentDetail.put("country", conference.get(4));
			//documentDetail.put("preceedingTitle", conference.get(5));
			documentDetail.put("keywords", conference.get(5));
			
			document.put("detail", documentDetail);

			//docResearchers.insertOne(documentDetail);
		}

	}
}



// Cuando se pone Conunicacion en congreso/Ponencia en jornada en el campo conference
// Quitar ) final del campo country
// Dejar los campos vacios cuando el datos no corresponde (Ej: city = 2012)
// Poner el año correcto (el que está justo detrás del country)