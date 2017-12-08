package src;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class baidu {
	private static final String ACCEPT = "text/html, */*; q=0.01";
    private static final String ENCODING = "gzip, deflate";
    private static final String LANGUAGE = "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3";
    private static final String CONNECTION = "keep-alive";
    private static final String HOST = "www.baidu.com";
    private static final String HOST_zhidao = "zhidao.baidu.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:31.0) Gecko/20100101 Firefox/31.0";

    private List<Evidence> searchZhidao(String url, String referer) {
        List<Evidence> evidences = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url)
                    .header("Accept", ACCEPT)
                    .header("Accept-Encoding", ENCODING)
                    .header("Accept-Language", LANGUAGE)
                    .header("Connection", CONNECTION)
                    .header("User-Agent", USER_AGENT)
                    .header("Host", HOST_zhidao)
                    .header("Referer", referer)
                    .get();
            //System.out.println(document.html());
            Elements ListTit = document.getElementsByAttributeValue("class","dt mb-4 line");          
            Elements ListSnp = document.getElementsByAttributeValue("class","dd answer");
            for (int i = 2 ; i <= 7 ; i ++) {
            	Evidence evidence = new Evidence();
            	evidence.title = ListTit.get(i).text();
            	evidence.snippet = ListSnp.get(i).text();
            	System.out.println(evidence.title);
            	System.out.println(evidence.snippet);
            	evidence.source = 2 ;
            	evidence.score = 1 + 1/i ;
                evidences.add(evidence) ;
            }
        }catch (Exception ex) {
            	System.out.println("搜索出错");
            }
            return evidences;
     }
    
    private List<Evidence> searchBaidu(String url, String referer) {
        List<Evidence> evidences = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url)
                    .header("Accept", ACCEPT)
                    .header("Accept-Encoding", ENCODING)
                    .header("Accept-Language", LANGUAGE)
                    .header("Connection", CONNECTION)
                    .header("User-Agent", USER_AGENT)
                    .header("Host", HOST)
                    .header("Referer", referer)
                    .get();
            String resultCssQuery = "html > body > div > div > div > div > div";
            Elements elements = document.select(resultCssQuery);
            double i = 0 ;
            for (Element element : elements) {
            	i ++ ;
                Elements subElements = element.select("h3 > a");
                if(subElements.size() != 1){
                    System.out.println("没有找到标题");
                    i -- ;
                    continue;
                }
                String title =subElements.get(0).text();
                if (title == null || "".equals(title.trim())) {
                	System.out.println("标题为空");
                    continue;
                }
                subElements = element.select("div.c-abstract");
                if(subElements.size() != 1){
                	System.out.println("没有找到摘要");
                    continue;
                }
                String snippet =subElements.get(0).text();
                if (snippet == null || "".equals(snippet.trim())) {
                	System.out.println("摘要为空");
                    continue;
                }
                Evidence evidence = new Evidence();
                evidence.title = title;
                evidence.snippet = snippet;
                //System.out.println(title) ;
                System.out.println(snippet) ;
                evidence.source = 1 ;
                evidence.score = 1 + 1/i ;
                evidences.add(evidence);
            }
        } catch (Exception ex) {
        	System.out.println("搜索出错");
        }
        return evidences;
    }
	
	//public static void main(String args[]) throws UnsupportedEncodingException {
    public static List<Evidence> searchWeb(String query) throws UnsupportedEncodingException {	
        String uquery = new String(query.toString().getBytes("UTF-8"));   
        uquery = URLEncoder.encode(uquery, "UTF-8");
        String qzhidao = "http://zhidao.baidu.com/search?lm=0&rn=10&pn=1&fr=search&ie=utf-8&word="+ uquery ; 
        String qbaidu = "http://www.baidu.com/s?tn=monline_5_dg&ie=utf-8&wd=" + query+"&oq="+query+"&usm=3&f=8&bs="+query+"&rsv_bp=1&rsv_sug3=1&rsv_sug4=141&rsv_sug1=1&rsv_sug=1&pn=" + 1;
		String referer = "http://zhidao.baidu.com/"; 
		baidu seacher = new baidu() ;
		List<Evidence> baiduevd = seacher.searchBaidu(qbaidu, referer) ;
		List<Evidence> zhidaoevd = seacher.searchZhidao(qzhidao, referer) ;
		zhidaoevd.addAll(baiduevd) ;
		return zhidaoevd ;
	}
}
