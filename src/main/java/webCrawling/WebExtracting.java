package webCrawling;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import webCrawling.website.BlockchainNews;
import webCrawling.website.Cnbc;
import webCrawling.website.Coindesk;
import webCrawling.website.Website;

public class WebExtracting {
	
	/*
	 * Phương thức nhận tham số là đối tượng Website và trả về các danh sách các đối tượng bài viết trong các Website đó
	 * Các bài viết được lấy về là các bài viết được đăng sau thời gian cập nhật gần nhất 
	 */
	public List<Article> extractToArticles(Website web) throws IOException, ParseException {
		List<Article> articles = new ArrayList<>();
		
		System.out.println(web.getLastestUpdateTime());
		Document outerPage = Jsoup.connect(web.getWebLink()).userAgent("Mozilla").get();
		breakLabel:
		while(true) {
			List<String> articleLinks = web.getArticleLinks(outerPage);
			for(String articleLink: articleLinks) {
				Document page = Jsoup.connect(articleLink).userAgent("Mozilla").get();
				//Nếu thời gian đăng bài trước thời gian cập nhật gần nhất thì kết thúc quá trình trích xuất dữ liệu
				if(web.getDate(page).isAfter(web.getLastestUpdateTime())) {
					Article article = new Article(articleLink,
												  web.getName(),
												  web.getArticleType(),
												  web.getArticleTitle(page),
												  web.getArticleSummary(page),
												  web.getDetailedArticleContent(page),
												  web.getDate(page),
												  web.getHashtags(page),
												  web.getAuthorName(page));
					articles.add(article);
				} else break breakLabel;
			}
			outerPage = web.nextPage(outerPage);
			if(outerPage == null) break;
		}
		//Lưu thời gian hiện tại làm thời gian cập nhật gần nhất
		web.setLastestUpdateTime(LocalDate.now());
	
		return articles;
	}
	
	/*
	 * Phương thức lấy tham số là danh sách các đối tượng bài viết rồi in ra file articles.json
	 */
	@SuppressWarnings("unchecked")
	public void returnToJSONFile(List<Article> articles) throws FileNotFoundException, IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArticles = (JSONArray) jsonParser.parse(new FileReader(".\\src\\main\\resources\\articles.json"));
		
		for(Article article: articles) jsonArticles.add(article.convertToJSONObject());
		FileWriter writer = new FileWriter(".\\src\\main\\resources\\articles.json");
		writer.write(jsonArticles.toJSONString());
		writer.close();
	}
	
	/*
	 * Phương thức gửi danh sách các đối tượng bài viết dưới dạng JSON đến local host sử dụng phương thức PUT
	 * Tham số thứ hai là URL của local host
	 */
	@SuppressWarnings("unchecked")
	public void putDataToURL(List<Article> articles, String destination) throws URISyntaxException, IOException {
		URI uri = new URI(destination);
		URL url = uri.toURL();
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
        connection.setUseCaches(false);
        
        JSONArray jsonArticles = new JSONArray();
        for(Article article: articles) jsonArticles.add(article.convertToJSONObject());
        
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(jsonArticles.toJSONString());
        outputStream.flush();
        outputStream.close();
        
        // Nhận và in ra thông tin phản hồi từ phía local host
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        System.out.println("Response: " + response.toString());
        
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, URISyntaxException {
		WebExtracting we = new WebExtracting();
 		List<Article> articles = we.extractToArticles(new Cnbc()); //2021
//		we.putDataToURL(articles, "http://localhost:3000/data");
		we.returnToJSONFile(articles);
 		List<Article> articles2 = we.extractToArticles(new Coindesk()); //2018
//		we.putDataToURL(articles2, "http://localhost:3000/data");
		we.returnToJSONFile(articles2);
		List<Article> articles3 = we.extractToArticles(new BlockchainNews()); //2023
//		we.putDataToURL(articles3, "http://localhost:3000/data");
		we.returnToJSONFile(articles3);
		System.out.println("Done!!!");
	}
}
