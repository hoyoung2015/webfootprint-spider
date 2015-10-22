package net.hoyoung.spider.gzcy.org;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hoyoung on 2015/10/22.
 */
public class HtmlDetailParser {
    private static String join(String[] arr,String delemeter){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            if(i>0){
                sb.append(delemeter);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        Html html = new Html(FileUtils.readFileToString(new File("data/gzcy.org/gzcy_org_detail.html"),"UTF-8"));

        //评价范围
        String eval_range = html.xpath("//div[@class=hpcompany_det]/p[2]/text()").get().replaceAll("\\u00A0", " ").replace("评价范围：","").replace("   ",",").trim();
        String t_contact = html.xpath("//div[@class=hpcompany_det]/p[3]/text()").get().replaceAll("\\u00A0", " ").trim();

        System.out.println(t_contact);
    }
}
