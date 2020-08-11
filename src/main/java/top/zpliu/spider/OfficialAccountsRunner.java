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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OfficialAccountsRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
//        String url = "https://www.baikemy.com/disease/list/23/0?diseaseContentType=A";
        String url = "https://www.vreadtech.com/account_wzhxlx";
        XSSFWorkbook wb = new XSSFWorkbook();   //
        XSSFSheet se = wb.createSheet();  //sheet
        getListPage(url, wb, se);
        File temp = new File(UUID.randomUUID().toString().substring(0, 5) + ".xlsx");
        FileOutputStream outputStream = new FileOutputStream(temp);
        wb.write(outputStream);  //流写进去
    }

    private void getListPage(String url, XSSFWorkbook wb, XSSFSheet se) throws InterruptedException {
        Document doc = null;
        try {
            //      Proxy proxy = new Proxy(Proxy.Type.HTTP,new
            // InetSocketAddress("202.115.142.147",9200));       设置代理
            //      doc = Jsoup.connect(url).proxy(proxy).get();
            System.setProperty("java.net.useSystemProxies", "true");    //代理软件，自动进行系统代理 全局代理
            doc = Jsoup.connect(url).timeout(20000).get();  //html->java对象 操作
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
        Element body = doc.body();
        Elements elements = body.getElementsByClass("list-group-item");
        int titleid=1;
        for (Element element : elements) {
            Element title = element.getElementsByTag("a").get(0);
            if (title.attr("href").contains("javascript")) {
                continue;
            }
            int rowNum = se.getLastRowNum() + 1;
            XSSFRow row = se.createRow(rowNum);
            XSSFCell cell = row.createCell(1);
            cell.setCellValue(title.text());
            XSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(titleid);
            titleid++;
            System.out.println(title.html() + "\t" + title.attr("href") + "\t");    //自我挫败型人格障碍	/disease/detail/42968710839553

//            List<Detail> details = getDetailPage("https://www.baikemy.com" + title.attr("href"), row);   //list
            List<Detail> details = getDetailPage(title.attr("href"), row);
            for (Detail detail : details) {
                XSSFCell t = row.createCell(row.getLastCellNum() + 1);
                se.setColumnWidth(row.getLastCellNum() + 1, 30 * 256);
                t.setCellValue(detail.title);
                t.setCellStyle(xssfCellStyle);
                List<String> strings = detail.content;
                int nextCellNum = row.getLastCellNum();
                se.setColumnWidth(nextCellNum, 30 * 256);
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

    private List<Detail> getDetailPage(String url, XSSFRow row) throws InterruptedException {
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
        Elements content = body.getElementById("popularVersion").children();
        for (Element element : content) {
            String title = element.getElementsByClass("p_directory_flag").get(0).text();
            List<String> strings =
                    element.getElementsByTag("p").stream()
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList());
            strings.addAll(
                    element.getElementsByTag("li").stream()
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList())
            );
            Detail detail = new Detail();
            detail.title = title;
            detail.content = strings;
            details.add(detail);
        }

        Elements content2 = body.getElementById("specialityVersion").children();
        for (Element element : content2) {
            String title = element.getElementsByClass("s_directory_flag").get(0).text();
            List<String> strings =
                    element.getElementsByTag("p").stream()
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList());
            strings.addAll(
                    element.getElementsByTag("li").stream()
                            .map(element1 -> element1.text())
                            .collect(Collectors.toList())
            );
            Detail detail = new Detail();
            detail.title = title;
            detail.content = strings;
            details.add(detail);
        }

        return details;
    }

    class Detail {
        List<String> content;
        String title;
    }
}