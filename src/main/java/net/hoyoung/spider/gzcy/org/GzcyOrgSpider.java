package net.hoyoung.spider.gzcy.org;

import net.hoyoung.spider.JDBCHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hoyoung on 2015/10/22.
 */
public class GzcyOrgSpider implements PageProcessor {
    static JdbcTemplate jdbcTemplate = null;
    static {
        jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                "jdbc:mysql://localhost/green_org?useUnicode=true&characterEncoding=utf8",
                "root", "", 5, 30);
//        jdbcTemplate.update("delete * from gzcy_org where 1=1");
    }
    Pattern certPattern = Pattern.compile("证书等级：([\\u4E00-\\u9FA5]{2}).*证书编号：([A-Z0-9]+)");
    public void process(Page page) {

        List<Selectable> trs = page.getHtml().xpath("//table[@class=listbox]/tbody/tr").nodes();

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
            int status = jdbcTemplate.update("INSERT INTO gzcy_org(id,name,province,city,area,addr,cert_level,cert_num) VALUES (?,?,?,?,?,?,?,?)",
                    id,
                    name,
                    province,
                    city,
                    area,
                    addr,
                    cert_level,
                    cert_num);
            if(status==1){
                System.out.println(id+"|"+name+" 插入成功");
            }
        }
    }

    public static void main(String[] args) {

        Spider spider = Spider.create(new GzcyOrgSpider())
                .thread(2)
                .setScheduler(new FileCacheQueueScheduler("data"));
        for (int i = 1; i <=79 ; i++) {
            spider.addUrl("http://www.gzcy.org/?m=home&c=unit&a=lists&page=" + i);
        }
        spider.run();
    }
    private Site site = Site.me().setRetryTimes(5).setSleepTime(600)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
    public Site getSite() {
        return site;
    }
}
