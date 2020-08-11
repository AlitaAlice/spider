package top.zpliu.spider;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static top.zpliu.spider.util.DownloadPicFromURL.downloadPicture;

/**
 * Title:
 * Description:
 * Company: http://www.biyouxinli.com/
 *
 * @author zhangxl@biyouxinli.com
 * @date Created in 12:01 2020/8/11
 */
@Component
public class CaoLiuRunner implements CommandLineRunner {

    public  static      List<String> urlList = new ArrayList<>();
    public  static      List<String> imgIndexList = new ArrayList<>();
    @Override
    public void run(String... args) throws Exception {


        getImg();
       // getHref();
    }
    public  void getImgIndex() {
        for (int i = 6; i <100 ; i++) {
            imgIndexList.add("https://cc.euuwr.com/thread0806.php?fid=16&search=&page=" + i);
        }
    }
    private  void getHref( Document doc) {
        Element body = doc.body();
        Elements elements = body.getElementsByClass("tr3 t_one tac");
        for (Element element : elements) {

            Element title = element.getElementsByTag("a").get(0);
            if (title.attr("href").contains("javascript"))        //获得title
            {
                continue;
            }
            String indexUrl = "https://cc.euuwr.com/";
            urlList.add(indexUrl + title.attr("href"));
        }
        for (int i = 0; i <9 ; i++) {
            urlList.remove(0);
        }

    }
    private void getImg() {
        Document doc = null;
        getImgIndex();
        int filename = 258;
        int name = 1;
        for (String indexUrl : imgIndexList) {
            try {
                System.setProperty("java.net.useSystemProxies", "true");
                //String url = "https://cc.euuwr.com/thread0806.php?fid=16&search=&page=1";
                Connection connection = Jsoup.connect(indexUrl);
                connection.userAgent("Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                doc = connection.timeout(20000).get();
//            doc = Jsoup.connect(url).timeout(20000).get();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
         getHref(doc);
//        Elements content = body.getElementById("tr1 do_not_catch").children();

        for (String url : urlList) {
            Document doc2 = null;
            try {
                System.setProperty("java.net.useSystemProxies", "true");
                Connection connection = Jsoup.connect(url);
                connection.userAgent("Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                doc = connection.timeout(20000).get();
//            doc = Jsoup.connect(url).timeout(20000).get();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Element   body2 = doc.body();
            Elements imglink = body2.select("img");
            ArrayList<String> imglist = new ArrayList<>();
            int i = 0;
            for (Element element : imglink) {
                String src = element.attr("ess-data");
                imglist.add(i, src);
                i++;
            }
            System.out.println(imglist.toString());
            String path = "E:/photo/";
            for (String imgurl : imglist) {
//                downloadPicture(imgurl, path + filename +"/"+ name + ".jpg");
                downloadPicture(imgurl, path  + name + ".jpg");
                System.out.printf(" 第 %s 个网站，下载第 %s 张图片完成", filename,name );
                name++;
            }
            filename++;
        }
    }
    }
}
