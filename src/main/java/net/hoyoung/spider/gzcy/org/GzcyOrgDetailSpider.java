package net.hoyoung.spider.gzcy.org;

import net.hoyoung.spider.JDBCHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hoyoung on 2015/10/22.
 */
public class GzcyOrgDetailSpider implements PageProcessor {
    static JdbcTemplate jdbcTemplate = null;
    static {
        jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                "jdbc:mysql://localhost/green_org?useUnicode=true&characterEncoding=utf8",
                "root", "", 5, 30);
    }
    Pattern certPattern = Pattern.compile("证书等级：([\\u4E00-\\u9FA5]{2}).*证书编号：([A-Z0-9]+)");
    public void process(Page page) {
        System.out.println(page.getHtml().toString());
    }

    public static void main(String[] args) {

        Spider spider = Spider.create(new GzcyOrgDetailSpider())
                .thread(2)
                .setScheduler(new FileCacheQueueScheduler("data"));
        List<String> ids = jdbcTemplate.queryForList("SELECT id FROM gzcy_org", String.class);
        for (String id : ids) {
            spider.addUrl("http://www.gzcy.org/?m=home&c=unit&a=info&id=" + id);
            break;
        }
        spider.run();

    }
    private Site site = Site.me().setRetryTimes(5).setSleepTime(500)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
    public Site getSite() {
        return site;
    }
}
