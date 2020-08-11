package top.zpliu.spider;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.zpliu.spider.util.DownloadPicFromURL;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static top.zpliu.spider.util.DownloadPicFromURL.downloadPicture;

//@Component
public class VRead2Runner implements CommandLineRunner {
    static int filename=1;
    static int index=1;
    @Override
    public void run(String... args) throws Exception {
        /*   String url = "https://chuansongme.com/account/shiyi201633?start=0";*/
        ArrayList<String> urllist = new ArrayList<>();  //导航界面的url
        /*        urllist.add("https://chuansongme.com/account/shiyi201633?start=0");*/
        for (int navigationvalue = 1; navigationvalue < 3 ; navigationvalue += 1) {  //对应导航
            String navigationurl = "https://www.vreadtech.com/account_wzhxlx&page=";
            String navigationurl_new = navigationurl + navigationvalue;
            System.out.println(navigationurl_new);
            urllist.add(navigationurl_new);
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet se = wb.createSheet();
        /*  XSSFSheet se2 = wb.createSheet();*/
        for (String url : urllist) {
            getListPage(url, wb, se);    //获得导航对应url 并获得对应detail 详细信息
        }
        File temp = new File(UUID.randomUUID().toString().substring(0, 5) + ".xlsx");
        FileOutputStream outputStream = new FileOutputStream(temp);
        wb.write(outputStream);
    }

    public static HashMap<String, String> convertCookies(String cookie) {
        HashMap<String, String> cookiesMap = new HashMap<>();
        String[] items = cookie.trim().split(";");
        for (String item : items) cookie.trim().split(";");
        for (String item : items) cookiesMap.put(item.split("=")[0], item.split("=")[1]);   //=号进行拆分 第一个元素 第二个元素
        return cookiesMap;
    }

    private void getListPage(String url, XSSFWorkbook wb, XSSFSheet se) throws InterruptedException {
        Document doc = null;
        try {
            //      Proxy proxy = new Proxy(Proxy.Type.HTTP,new
            // InetSocketAddress("202.115.142.147",9200));
            //      doc = Jsoup.connect(url).proxy(proxy).get();
            System.setProperty("java.net.useSystemProxies", "true");
            String cookie = "access_token=34_oF4RyiDlsyz_O9U120ACzRVv6kpowZJEtkj9Hbr7FHK7mQCGXkR8-iqoYx6q7bZ3A8KTPhJNYpeGHGdLBeGQcqPUfWW7I4kAico0miLYSbwCx_kvfaVI8YpSJ7I0gbkm8X1XaspYXOzA2axnNDObAEAXNY; etime=2020-06-17+21%3A06%3A31%2B1week; ulevel=0; headimgurl=http%3A//thirdwx.qlogo.cn/mmopen/NLbUFBy5nMCw25vYicZtxZvia8nwQZNQOfclPbmVuNtdUbnQ23yTWD8ibkCWJdmsT09Vdhh5ticqG4Ml3O5lJqxzzBO2tWRl0Wvx/132; nickname=Alita; openid=obyEs1UJ9vkJtHLRRlkvoC6ceUOI";
            doc = Jsoup.connect(url).cookies(convertCookies(cookie)).timeout(20000).get();  //html->java对象 操作
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        XSSFCellStyle xssfCellStyle = wb.createCellStyle();
        xssfCellStyle.setFont(font);

        XSSFCellStyle contentStyle = wb.createCellStyle();
        contentStyle.setAlignment(HorizontalAlignment.FILL);
/*        Element body2 = doc.body(); 暂时不取导航href
        Element navigationelement = body2.getElementById("w4_5");*/

        Element body = doc.body();
        Elements elements = body.getElementsByClass("hbox stretch main-l");  //列表循环
        for (Element element : elements) {
            Element title = element.getElementsByClass("clear tit-a link_valid").get(0);
            if (title.attr("href").contains("javascript"))        //获得title
            {
                continue;
            }
            int rowNum = se.getLastRowNum() + 1;
            XSSFRow row = se.createRow(rowNum);
            XSSFCell cell2 = row.createCell(1);
            cell2.setCellValue(index);
            index++;
            System.out.println(index);
            XSSFCell cell = row.createCell(2);
            cell.setCellValue(title.text());
            XSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(title.attr("href"));
            System.out.println(title.html() + "\t" + title.attr("href") + "\t");
            List<Detail> details = getDetailPage(title.attr("href"), row,filename);        //获得每篇对应href去getDetailPage 获得每篇的信息
            filename++;
            System.out.println(filename);
            for (Detail detail : details) {
                XSSFCell t = row.createCell(row.getLastCellNum() + 1);
                se.setColumnWidth(row.getLastCellNum() + 1, 40 * 256);
                t.setCellValue(detail.title);
                t.setCellStyle(xssfCellStyle);
                List<String> strings = detail.content;
                int nextCellNum = row.getLastCellNum();
                se.setColumnWidth(nextCellNum, 40 * 256);
                for (int i = 0; i < strings.size(); i++) {
                    XSSFRow seRow = se.getRow(rowNum + i);
                    if (Objects.isNull(seRow)) {
                        seRow = se.createRow(rowNum + i);
                    }
                    XSSFCell seRowCell = seRow.createCell(nextCellNum);
                    seRowCell.setCellStyle(contentStyle);
                    seRowCell.setCellValue(strings.get(i));
                }
            }


            Thread.sleep(new Random().nextInt(1000) + 2000);
        }

    }

    private List<Detail> getDetailPage(String url, XSSFRow row,int filename) throws InterruptedException {
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
        List<Detail> details = new ArrayList<>();

        if (doc == null) {
            return details;
        }

        Element body = doc.body();
        Elements content2 = body.getElementById("img-content").children();
        Elements imglink = content2.select("img[src]");
        ArrayList<String> imglist = new ArrayList<>();
        int i = 0;
        for (Element element2 : imglink) {
            String src = element2.attr("src");
            imglist.add(i, src);
            i++;
        }
        System.out.println(imglist);
        String path = "E:/jpg/";
        int name = 1;
        for (String imgurl : imglist) {
            downloadPicture(imgurl, path + filename +"/"+ name + ".jpg");
            name++;
        }

        Elements content = body.getElementById("img-content").children();
        for (Element element : content) {
//            String title = element.getElementsByClass("s_directory_flag").get(0).text();
            List<String> strings =
                    element.getElementsByTag("p").stream()               //lambda
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList());
            strings.addAll(
                    element.getElementsByTag("section").stream()
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList())
            );
            Detail detail = new Detail();
//            detail.title = title;
            detail.content = strings;
            details.add(detail);
        }


        return details;
    }

    class Detail {
        List<String> content;
        String title;
        List<String> imglink;
    }

}
