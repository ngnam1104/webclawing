package webCrawling.website;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BlockchainNews implements Website {

    private String webName;
	private String webLink;
	private String articleType;
	private LocalDate lastestUpdateTime;

    public BlockchainNews() throws FileNotFoundException, IOException, ParseException{
        webName = "Blockchain News";
        webLink = "https://blockchain.news/tag/Cryptocurrency";
        //https://blockchain.news/tag/Industry : FINRA : Không chuyên sâu về công nghệ Blockchain nhưng liên quan đến kinh tế nhiều hơn
        //Thiếu thì lấy thêm ở đó
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

    @Override
    public List<String> getArticleLinks(Document outerPage) {
        List<String> links = new ArrayList<>();
		Elements titles = outerPage.select("h4.entry-title");
		for(Element title: titles){
            Element link = title.selectFirst("a");
            if(link != null){
                String nextLink = link.attr("abs:href");
                if(nextLink.indexOf("\"") == -1)
                    links.add(nextLink);
            }      
        } 
		return links;
    }

    @Override
    public Document nextPage(Document outerPage) throws IOException {
        String linkToNextPage = outerPage.select("a:containsOwn(Next)").attr("abs:href");
        if(linkToNextPage == "" || linkToNextPage.indexOf("\"") != -1) return null;
		System.out.println(linkToNextPage);
        Document nextPage = Jsoup.connect(linkToNextPage).userAgent("Mozilla").get();
        return nextPage;
    }

	/*
	* WORK WITH ARTICLE PAGE
	*/

    @Override
    public LocalDate getDate(Document page) {
        String time = page.select(".sidebar.col-sm-4 .entry-date").text();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        LocalDate localDate = LocalDate.parse(time, formatter);
        return localDate;
    }

    @Override
    public String getArticleTitle(Document page) {
        Elements titles = page.select("h2.title");
		return titles.text();
    }

    @Override
    public String getArticleSummary(Document page) {
        Elements summary = page.select("[class=\"text-size-big\"]");
		return summary.text();
    }

    @Override
    public String getDetailedArticleContent(Document page) {
        Elements detailedContent = page.select("[class=\"textbody\"]");
		return detailedContent.text();
        
    }

    @Override
    public Set<String> getHashtags(Document page) {
        Set<String> hashTags = new HashSet<>();
		Elements listHashTags = page.select("[class=\"tagcloud\"]");
		for(Element hashTag: listHashTags){
			hashTags.add(hashTag.text().toLowerCase());
		}
		return hashTags;
    }

    @Override
    public String getAuthorName(Document page) {
        Elements authorName = page.select("[class=\"entry-cat\"]");
		return authorName.text();
    }
  
    /*
    * GETTER, SETTER
    */

    
    @Override
    public String getName() {
        return webLink;
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
    
    @Override
    public String getArticleType() {
        return articleType;
    }

    @Override
    public void setArticleType(String type) {
        articleType = type;
        
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

        FileWriter file = new FileWriter(".\\src\\main\\resources\\lastestUpdateTime.json");
        file.write(JSONObject.toJSONString(jsonData));
        file.close();
        
    }
    // public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
	// 	BlockchainNews test = new BlockchainNews();
	// 	Document document = Jsoup.connect("https://blockchain.news/news/report-by-bitget-reveals-1-5-million-daily-active-crypto-traders-in-western-europe").userAgent("Mozilla").get();
	// 	Document outer = Jsoup.connect("https://blockchain.news/tag/Cryptocurrency").userAgent("Mozilla").get();
	// 	System.out.println("ArticleType: " + test.getArticleType());
	// 	System.out.println();
	// 	System.out.println("ArticleLinks: " +test.getArticleLinks(outer));
    //     System.out.println();
    //     System.out.println("getDate: " + test.getDate(document));
    //     System.out.println();
    //     System.out.println("ArticleTitle: " + test.getArticleTitle(document));
    //     System.out.println();		
    //     System.out.println("ArticleSummary: " + test.getArticleSummary(document));
	// 	System.out.println();
	// 	System.out.println("DetailedArticleContent: " + test.getDetailedArticleContent(document));
	// 	System.out.println();
	// 	System.out.println("Hashtags: " + test.getHashtags(document));
	// 	System.out.println();
	// 	System.out.println("WebLink: " + test.getWebLink());
	// 	System.out.println();
	// 	System.out.println("AuthorName: " + test.getAuthorName(document));
	// 	System.out.println();
	// 	System.out.println("nextPage: " + test.nextPage(outer));
		
	// }
}
