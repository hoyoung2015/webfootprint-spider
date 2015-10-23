package net.hoyoung.spider.gzcy.org;

import net.hoyoung.spider.JDBCHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
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
        //评价范围
        String eval_range = page.getHtml().xpath("//div[@class=hpcompany_det]/p[2]/text()").get().replaceAll("\\u00A0", " ").replace("评价范围：","").replace("   ",",").trim();
        String t_contact = page.getHtml().xpath("//div[@class=hpcompany_det]/p[3]/text()").get().replaceAll("\\u00A0", " ").trim();

        Pattern contactPattern = Pattern.compile("联系方式：([0-9-]*)    邮箱：(.*)");
        Matcher m = contactPattern.matcher(t_contact);
        String tel = null;
        String email = null;
        if(m.find()){
            tel = m.group(1);
            email = m.group(2);
        }
        int project_num = 0;

        Pattern projectNumPattern =Pattern.compile("共 (\\d+) 条记录");
        m = projectNumPattern.matcher(page.getHtml().xpath("//div[@class=fr]/span[@class=span2]/text()").get());
        if(m.find()){
            project_num = Integer.parseInt(m.group(1));
        }

        /**
         * 证书持证人员
         * 几乎都是0，试着看有没有>0的
         */
        int permit_num = page.getHtml().xpath("//div[@class=wrap]/table[2]/tbody/tr").nodes().size();;


        /**
         * 环境影响评价工程师
         * 先判断有没有分页信息，如果有，直接取过来，如果没有，统计行数
         */
        int engineer_num = 0;
        String t_pager = page.getHtml().xpath("//li[@class=total_num]/a/text()").get();
        if(t_pager!=null){//存在分页
            engineer_num = Integer.parseInt(t_pager.substring(0,t_pager.indexOf("条")));
        }else{
            engineer_num = page.getHtml().xpath("//div[@class=wrap]/table[3]/tbody/tr").nodes().size();
        }

        /**
         * 违规记录
         */
        List<Selectable> list = page.getHtml().xpath("//div[@class=loglist]/ul/li").nodes();
        int record_num = 0;
        if(!"该环评单位暂无违规记录".equals(list.get(0).xpath("/li/text()").get())){//有违规记录
            record_num = list.size();
        }

        /**
         * 坐标
         */
        float pos_x = 0f;
        float pos_y = 0f;
        Pattern posPattern = Pattern.compile("map\\.centerAndZoom\\(new BMap\\.Point\\((\\d+\\.\\d+),(\\d+\\.\\d+)\\),13\\);");
        m = posPattern.matcher(page.getRawText());
        if(m.find()){
            pos_x = Float.parseFloat(m.group(1));
            pos_y = Float.parseFloat(m.group(2));
        }
        int status = jdbcTemplate.update("UPDATE gzcy_org SET eval_range=?,tel=?,email=?,project_num=?,permit_num=?,engineer_num=?,record_num=?,pos_x=?,pos_y=? WHERE id=?",
                eval_range,
                tel,
                email,
                project_num,
                permit_num,
                engineer_num,
                record_num,
                pos_x,
                pos_y,
                page.getRequest().getExtra("id"));
        if(status==1){
            System.out.println(page.getRequest().getExtra("count")+"|"+page.getRequest().getExtra("id")+" update successful");
        }
    }

    public static void main(String[] args) {
        System.out.println("爬虫启动");
        Spider spider = Spider.create(new GzcyOrgDetailSpider())
                .thread(2)
                .setScheduler(new FileCacheQueueScheduler("data"));
        List<String> ids = jdbcTemplate.queryForList("SELECT id FROM green_org.gzcy_org WHERE pos_x=0 OR pos_y=0;", String.class);
//        List<String> ids = new ArrayList<>();
//        ids.add("1184");
        int count = 1;
        System.out.println("注入url");
        for (String id : ids) {
            Request req = new Request("http://www.gzcy.org/?m=home&c=unit&a=info&id=" + id);
            req.putExtra("id",id);
            req.putExtra("count",count++);
            spider.addRequest(req);
        }
        System.out.println("开始爬取");
        spider.run();
    }
    private Site site = Site.me().setRetryTimes(5).setSleepTime(500)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
    public Site getSite() {
        return site;
    }
}
