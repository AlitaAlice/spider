package top.zpliu.spider.util;/**
 * Title:
 * Description:
 * Company: http://www.biyouxinli.com/
 *
 * @author zhangxl@biyouxinli.com
 * @date Created in 14:53 2020/6/19
 */

import java.io.File;

/**
 * @ClassName CreateFile
 * @Description TODO
 * @Author 16508
 * @Date 2020/6/19 14:53
 * @Version 1, 0
 **/
public class CreateFile {
    public static void main(String[] args) {
        String directory = "E:/jpg/";
        for(int i=1;i<=600;i++)
        {
        String directory_new = directory + i;
        File file = new File(directory_new);
        file.mkdir();
        }
    }

}
