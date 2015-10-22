package net.hoyoung.spider.gzcy.org;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by hoyoung on 2015/10/22.
 */
public class HtmlParser {
    public static void main(String[] args) throws IOException {
        Pattern certPattern = Pattern.compile("证书等级：([\\u4E00-\\u9FA5]{2}).*证书编号：([A-Z0-9]+)");

        Html html = new Html(FileUtils.readFileToString(new File("data/gzcy.org/eid_units.html"),"UTF-8"));
        List<Selectable> trs = html.xpath("//table[@class=listbox]/tbody/tr").nodes();

        for (Selectable tr : trs) {

            String t_id = tr.xpath("//td//a/@href").get();
            String id = t_id.substring(t_id.lastIndexOf("=") + 1);
            String name = tr.xpath("//td//a/text()").get();
            String addr = tr.xpath("//td//p[@class=p2]/text()").get().replaceAll("\\u00A0", " ");

            String[] addrs = addr.split(" ");
            String province = addrs[0];
            String city = addrs[1];
            String area = addrs[2];
            String cert_level = null;
            String cert_num = null;
            String t_cert = tr.xpath("//td[2]/text()").get();
            if(!StringUtils.isEmpty(t_cert)){
                Matcher m = certPattern.matcher(t_cert);
                if(m.find()){
                    cert_level = m.group(1);
                    cert_num = m.group(2);
                }
            }
        }
    }
}
