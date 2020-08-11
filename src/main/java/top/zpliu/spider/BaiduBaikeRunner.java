package top.zpliu.spider;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class BaiduBaikeRunner  implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        String url = "http://baike.baidu.com/fenlei/%E5%BF%83%E7%90%86%E5%AD%A6%E6%9C%AF%E8%AF%AD?limit=30&index=1&offset=3#gotoList";
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet se = wb.createSheet();
        getListPage(url, se);
        File temp = new File(UUID.randomUUID().toString().substring(0, 5) + ".xlsx");
        FileOutputStream outputStream = new FileOutputStream(temp);
        wb.write(outputStream);
    }

    private void getListPage(String url, XSSFSheet se) throws InterruptedException {
        Document doc = null;
        try {
//      Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("202.115.142.147",9200));
//      doc = Jsoup.connect(url).proxy(proxy).get();
            System.setProperty("java.net.useSystemProxies", "true");
            doc = Jsoup.connect(url).timeout(20000).get();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Element body = doc.body();
        Elements elements = body.getElementsByClass("grid-list");
        if(elements.size()<1){
            System.out.println("停止收集："+body.toString());
            return;
        }
        for (Element element : elements.get(0).getElementsByTag("li")) {
            Element title = element.getElementsByClass("title").get(0);
            XSSFRow row = se.createRow(se.getLastRowNum() + 1);
            XSSFCell cell = row.createCell(1);
            cell.setCellValue(title.text());
            XSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(title.attr("href"));
            XSSFCell cell3 = row.createCell(3);
            System.out.print(title.html() + "\t" + title.attr("href")+"\t");

            cell3.setCellValue(getDetailPage("https://baike.baidu.com"+  title.attr("href")));
            System.out.println(cell3.getStringCellValue());
            Thread.sleep(new Random().nextInt(1000) + 2000);
        }
        Element next = body.getElementById("next");
        if (Objects.nonNull(next)) {
            Thread.sleep(5000);
            getListPage("http://baike.baidu.com/fenlei/"+next.attr("href"), se);
        }
    }

    private String getDetailPage(String url) throws InterruptedException {
        Document doc = null;
        int count = 3;
        while (doc == null && --count > 0) {
            try {
                //      Proxy proxy = new Proxy(Proxy.Type.HTTP,new
                // InetSocketAddress("202.115.142.147",9200));
                //      doc = Jsoup.connect(url).proxy(proxy).get();
                doc = Jsoup.connect(url).timeout(20000).get();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if(doc == null){
            return "爬取超时";
        }
        Element body = doc.body();
        Elements content = body.getElementsByClass("lemma-summary");
        if(content.size() > 0){
            Element element = content.get(0);
            return element.text();
        }
        return "";
    }
}
