package top.zpliu.spider.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Title: <br>
 * Description: <br>
 * Company: http://www.biyouxinli.com/
 *
 * @author liuzp@biyouxinli.com
 * @date 2019-05-08 13:34
 */
@Slf4j
public class ProxySpiderUtil {
  ThreadLocal<Integer> localWantedNumber = new ThreadLocal<Integer>();
  ThreadLocal<List<ProxyInfo>> localProxyInfos = new ThreadLocal<List<ProxyInfo>>();

  public static void main(String[] args) {
    ProxySpiderUtil proxyCrawler = new ProxySpiderUtil();
    /** 想要获取的代理IP个数，由需求方自行指定。（如果个数太多，将导致返回变慢） */
    proxyCrawler.startCrawler(1);
  }
  /**
   * 暴露给外部模块调用的入口
   *
   * @param wantedNumber 调用方期望获取到的代理IP个数
   * @return
   */
  public List<ProxyInfo> startCrawler(int wantedNumber) {
    log.info("开始获取代理IP，目标数量：" + wantedNumber);
    localWantedNumber.set(wantedNumber);
//    kuaidailiCom("http://www.xicidaili.com/nn/", 2);
//    kuaidailiCom("http://www.xicidaili.com/nt/", 2);
    kuaidailiCom("http://www.xicidaili.com/wt/", 2);
//    kuaidailiCom("http://www.kuaidaili.com/free/inha/", 2);
//    kuaidailiCom("http://www.kuaidaili.com/free/intr/", 2);
//    kuaidailiCom("http://www.kuaidaili.com/free/outtr/", 2);
    return localProxyInfos.get();
  }

  private void kuaidailiCom(String baseUrl, int totalPage) {
    String ipReg = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} \\d{1,6}";
    Pattern ipPtn = Pattern.compile(ipReg);
    for (int i = 1; i < totalPage; i++) {
      if (getCurrentProxyNumber() >= localWantedNumber.get()) {
        return;
      }
      try {
        Document doc =
            Jsoup.connect(baseUrl + i + "/")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, sdch")
                .header("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
                .header("Cache-Control", "max-age=0")
                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
                .header("Cookie","Hm_lvt_7ed65b1cc4b810e9fd37959c9bb51b31=1462812244; _gat=1; _ga=GA1.2.1061361785.1462812244")
                .header("Host", "www.kuaidaili.com")
                .header("Referer", "http://www.kuaidaili.com/free/outha/")
                .timeout(10 * 1000)
                .get();
        Matcher m = ipPtn.matcher(doc.text());
        while (m.find()) {
          if (getCurrentProxyNumber() >= localWantedNumber.get()) {
            break;
          }
          String[] strs = m.group().split(" ");
          if (checkProxy(strs[0], Integer.parseInt(strs[1]))) {
            log.info("获取到可用代理IP\t" + strs[0] + "\t" + strs[1]);
            addProxy(strs[0], strs[1], "http");
          } else {
              log.info("代理IP不可用\t" + strs[0] + "\t" + strs[1]);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static boolean checkProxy(String ip, Integer port) {
    try {
      Jsoup.connect("http://zpliu.top/").timeout(5 * 1000).proxy(ip, port).get();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private int getCurrentProxyNumber() {
    List<ProxyInfo> proxyInfos = localProxyInfos.get();
    if (proxyInfos == null) {
      proxyInfos = new ArrayList<ProxyInfo>();
      localProxyInfos.set(proxyInfos);
      return 0;
    } else {
      return proxyInfos.size();
    }
  }

  private void addProxy(String ip, String port, String protocol) {
    List<ProxyInfo> proxyInfos = localProxyInfos.get();
    if (proxyInfos == null) {
      proxyInfos = new ArrayList<ProxyInfo>();
    }
    ProxyInfo proxyInfo = new ProxyInfo();
    proxyInfo.setIp(ip);
    proxyInfo.setPort(port);
    proxyInfo.setProtocol(protocol);
    proxyInfos.add((proxyInfo));
  }
}
