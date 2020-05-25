/**
 *
 * @author Pavlos Fafalios
 */
public class Article {
    
    private String url;
    private String title;
    private String headline;
    private String content;
    private String pubDate;

    public Article(String url, String title, String headline, String content, String pubData) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.pubDate = pubData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }
    
    
    
}
