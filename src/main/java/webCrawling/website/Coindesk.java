package webCrawling.website;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class  Coindesk implements Website{
    private String webName;
	private String webLink;
	private String articleType;
	private LocalDate lastestUpdateTime;

    public Coindesk() throws FileNotFoundException, IOException, ParseException{
        webName = "CoinDesk";
        webLink = "https://www.coindesk.com/tag/blockchains/";
        articleType = "News";
        lastestUpdateTime = getLastestUpdateTimeFromJSONFile();
    }

    private LocalDate getLastestUpdateTimeFromJSONFile() throws FileNotFoundException, IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(".\\src\\main\\resources\\lastestUpdateTime.json"));
		//jsonParser.parse(String jsonText) return an Object
		JSONObject jsonData = (JSONObject) obj;
		String dateStr = (String) jsonData.get(webName); //get(String key)
		LocalDate date = LocalDate.parse(dateStr);
		//parse(CharSequence text)
		return date;
	}

	/*
	* WORK WITH MAIN PAGE
	*/	

	public List<String> getArticleLinks(Document outerPage){
		List<String> links = new ArrayList<>();
		Elements titles = outerPage.select(".card-title[href][target]");
		for(Element title: titles) {
			String href = title.attr("abs:href");
        	if (!href.contains("/video/") && !href.contains("/podcast/") && href.indexOf("\"") == -1) {
            	links.add(href); 
				//trong link co video va podcast khong co bai bao, va mot so link co ki tu " lam cho khong chuyen thanh string duoc
        	}
		}
		return links;
	};

	@Override
	public Document nextPage(Document outerPage) throws IOException{
		String linkToNextPage = outerPage.select("a[aria-label='Next page']").attr("abs:href");
		if(linkToNextPage == "") return null;
		System.out.println(linkToNextPage);
		Document nextPage = Jsoup.connect(linkToNextPage).userAgent("Mozilla").get();
		return nextPage;
	};

	/*
	* WORK WITH ARTICLE PAGE
	*/

	@Override
	public LocalDate getDate(Document page) throws IndexOutOfBoundsException{
		String time = page.select("div > img[src] + span.typography__StyledTypography-sc-owin6q-0").text();
		if(time == "") return null;
		int index = time.indexOf(" at");
		if(index != -1)
			time = time.substring(0, index);
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
		try {
            // Thử chuyển đổi chuỗi thành đối tượng LocalDateTime
            LocalDate localDate = LocalDate.parse(time, inputFormatter);
            return localDate;
        } catch (DateTimeParseException e) {
            return LocalDate.of(9999, 1, 1);
		}
	}

	@Override
	public String getArticleTitle(Document page){
		Elements titles = page.select("[class=\"typography__StyledTypography-sc-owin6q-0 kbFhjp\"]");
		return titles.text();
	};	

	@Override
	public String getArticleSummary(Document page){
		Elements summary = page.select("[class=\"typography__StyledTypography-sc-owin6q-0 sVcXY\"]");
		return summary.text();
	};

	@Override
	public String getDetailedArticleContent(Document page){
		Elements detailedContent = page.select("[class=\"common-textstyles__StyledWrapper-sc-18pd49k-0 eSbCkN\"]:not(:has(i))");
		return detailedContent.text();
	};

	@Override
	public Set<String> getHashtags(Document page){
		Set<String> hashTags = new HashSet<>();
		Elements listHashTags = page.select("[class=\"Box-sc-1hpkeeg-0 jrrGDt\"] [class=\"article-tagsstyles__TagPill-sc-17t0gri-0 eJTFpe light\"]");
		for(Element hashTag: listHashTags){
			hashTags.add(hashTag.text().toLowerCase());
		}
		return hashTags;
	};

	@Override
	public String getAuthorName(Document page){
		Elements authorName = page.select("[class=\"typography__StyledTypography-sc-owin6q-0 dtjHgI\"][href]");
		return authorName.text();
	};
	
	/*
	* GETTER, SETTER
	*/

	@Override
	public String getName(){
		return webName;
	}; // getter

	@Override
	public void setName(String name){
        webName = name;
	}; // setter

	@Override
	public String getWebLink(){
		return webLink;
	};

	@Override
	public void setWebLink(String url){
        webLink = url;
	};

	@Override
	public String getArticleType(){
        return articleType;
	};

	@Override
	public void setArticleType(String type){
		articleType = type;
	};

	@Override
	public LocalDate getLastestUpdateTime(){
        return lastestUpdateTime;
	};

	@SuppressWarnings("unchecked")
	@Override
	public void setLastestUpdateTime(LocalDate date) throws FileNotFoundException, IOException, ParseException{
        lastestUpdateTime = date;
        JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(".\\src\\main\\resources\\lastestUpdateTime.json"));
		JSONObject jsonData = (JSONObject) obj;
        jsonData.put(webName, date.toString());

        FileWriter file = new FileWriter(".\\src\\main\\resources\\lastestUpdateTime.json");
        file.write(JSONObject.toJSONString(jsonData));
        file.close();
	};
	// public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
	// 	Coindesk test = new Coindesk();
	// 	Document outer = Jsoup.connect("https://www.coindesk.com/tag/blockchains/").userAgent("Mozilla").get();
	// 	List<String> list = test.getArticleLinks(outer);
	// 	System.out.println(test.nextPage(outer));
	// 	System.out.println("next: ");
	// 	for(String s : list){
	// 		Document document = Jsoup.connect(s).userAgent("Mozilla").get();
	// 		System.out.println(test.getArticleType());
	// 		System.out.println("next: ");
	// 		System.out.println(test.getArticleTitle(document));
	// 		System.out.println("next: ");
	// 		System.out.println(test.getArticleSummary(document));
	// 		System.out.println("next: ");
	// 		System.out.println(test.getDetailedArticleContent(document));
	// 		System.out.println("next: ");
	// 		System.out.println(test.getHashtags(document));
	// 		System.out.println("next: ");
	// 		System.out.println(test.getWebLink());
	// 		System.out.println("next: ");
	// 		System.out.println(test.getAuthorName(document));	
	// 		System.out.println("next: ");
	// 		System.out.println(test.getDate(document));
	// 		System.out.println("\n \n");
	// 		System.out.println("next page:");
	// 	}
		
	// }
} 	
