package webCrawling.website;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;

/*
 * Interface định nghĩa các phương thức trích xuất dữ liệu khi thực hiện web crawling
 * ví dụ: getArtitcleTitle() được dùng để lấy tiêu đề của bài viết
 */
public interface Website {
	/*
	 * Thời gian cập nhật gần nhất được lưu trong file lastestUpdateTime.json
	 */	

	/*
	* WORK WITH MAIN PAGE
	*/ 
	public List<String> getArticleLinks(Document outerPage);
	public Document nextPage(Document outerPage) throws IOException;

	/*
	* WORK WITH ARTICLE PAGE
	*/

	public LocalDate getDate(Document page);
	public String getArticleTitle(Document page);
	public String getArticleSummary(Document page);
	public String getDetailedArticleContent(Document page);
	public Set<String> getHashtags(Document page);
	public String getAuthorName(Document page);

	/*
	* GETTER, SETTER
	*/

	public LocalDate getLastestUpdateTime();
	public void setLastestUpdateTime(LocalDate date) throws FileNotFoundException, IOException, ParseException;
	public String getName(); // getter
	public void setName(String name); // setter
	public String getWebLink();
	public void setWebLink(String url);
	public String getArticleType();
	public void setArticleType(String type);
	
}
