package webCrawling.website;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * Class bao gồm các phương thức để trích xuất dữ liệu từ trang CNBC
 */
public class Cnbc implements Website {
	
	private static String webName;
	private static String webLink;
	private static String articleType;
	private static LocalDate lastestUpdateTime;
	
	public Cnbc() throws FileNotFoundException, IOException, ParseException {
		webName = "CNBC";
		webLink = "https://www.cnbc.com/blockchain/";
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
		
	@Override
	public LocalDate getLastestUpdateTime() {
		return lastestUpdateTime;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLastestUpdateTime(LocalDate date) throws FileNotFoundException, IOException, ParseException {
		lastestUpdateTime = date;
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(".\\src\\main\\resources\\lastestUpdateTime.json"));
		JSONObject jsonData = (JSONObject) obj;
		jsonData.put(webName, date.toString());
		
		FileWriter fileWriter = new FileWriter(".\\src\\main\\resources\\lastestUpdateTime.json");
		fileWriter.write(jsonData.toJSONString());
        fileWriter.close();
	}

	@Override
	public String getArticleType() {
		return articleType;
	}

	@Override
	public void setArticleType(String type) {
		articleType = type;
	}


	public List<String> getArticleLinks(Document outerPage) {
		List<String> links = new ArrayList<>();
		Elements titles = outerPage.select(".Card-title");
		for(Element title: titles){
			String nextLink = title.attr("abs:href");
                if(nextLink.indexOf("\"") == -1)
                    links.add(nextLink);
		}
		return links;
	}

	@Override
	public Document nextPage(Document outerPage) throws IOException {
		String linkToNextPage = outerPage.select(".LoadMoreButton-loadMore").attr("abs:href");
		if(linkToNextPage == "") return null;
		System.out.println(linkToNextPage);
		Document nextPage = Jsoup.connect(linkToNextPage).userAgent("Mozilla").get();
		return nextPage;
	}

	@Override 
	public LocalDate getDate(Document page) {
		LocalDate date = null;
		Elements titles = page.select("[property=\"article:published_time\"]");
		for(Element title: titles) {
			String dateTime = title.attr("content");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
			ZonedDateTime zDateTime = ZonedDateTime.parse(dateTime, formatter);
			date = zDateTime.toLocalDate();
		}
		return date;
	}

	@Override
	public String getArticleTitle(Document page) {
		String articleTitle = null;
		Elements titles = page.select("[class=\"ArticleHeader-headline\"]");
		for(Element title: titles) articleTitle = title.text();
		return articleTitle;
	}

	@Override
	public String getArticleSummary(Document page) {
		Elements titles = page.select(".RenderKeyPoints-list");
		return titles.text();
	}

	@Override
	public String getDetailedArticleContent(Document page) {
		Elements titles = page.select(".ArticleBody-articleBody");
		return titles.text();
	}

	@Override
	public Set<String> getHashtags(Document page) {
		return null;
	}

	@Override
	public String getAuthorName(Document page) {
		Elements titles = page.select(".Author-authorName");
		return titles.text();
	}

	
	@Override
	public String getName() {
		return webName;
	}

	@Override
	public void setName(String name) {
		webName = name;
	}
	
	@Override
	public String getWebLink() {
		return webLink;
	}

	@Override
	public void setWebLink(String url) {
		webLink = url;
	}

}
