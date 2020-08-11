package top.zpliu.spider.util;

import lombok.Data;
import java.util.Date;

/**
 * @program: gecco
 * @description:
 * @author: liuzp
 * @create: 2019-01-15 09:51
 **/
@Data
public class ProxyInfo {
    private Integer id;
    private String ip;
    private String port;
    private String protocol;
    private String address;
    private Integer liveDays;
    private Date addTime;
    private Date validTime;
    private String status;
}
