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
//        Html html = new Html(FileUtils.readFileToString(new File("data/gzcy.org/gzcy_org_detail_with_pager.html"),"UTF-8"));

        //评价范围
        String eval_range = html.xpath("//div[@class=hpcompany_det]/p[2]/text()").get().replaceAll("\\u00A0", " ").replace("评价范围：","").replace("   ",",").trim();
        String t_contact = html.xpath("//div[@class=hpcompany_det]/p[3]/text()").get().replaceAll("\\u00A0", " ").trim();

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
        m = projectNumPattern.matcher(html.xpath("//div[@class=fr]/span[@class=span2]/text()").get());
        if(m.find()){
            project_num = Integer.parseInt(m.group(1));
        }

        /**
         * 证书持证人员
         * 几乎都是0，试着看有没有>0的
         */
        int permit_num = html.xpath("//div[@class=wrap]/table[2]/tbody/tr").nodes().size();;


        /**
         * 环境影响评价工程师
         * 先判断有没有分页信息，如果有，直接取过来，如果没有，统计行数
         */
        int engineer_num = 0;
        String t_pager = html.xpath("//li[@class=total_num]/a/text()").get();
        if(t_pager!=null){//存在分页
            engineer_num = Integer.parseInt(t_pager.substring(0,t_pager.indexOf("条")));
        }else{
            engineer_num = html.xpath("//div[@class=wrap]/table[3]/tbody/tr").nodes().size();
        }

        /**
         * 违规记录
         */
        List<Selectable> list = html.xpath("//div[@class=loglist]/ul/li").nodes();
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
        m = posPattern.matcher(html.get());
        if(m.find()){
            pos_x = Float.parseFloat(m.group(1));
            pos_y = Float.parseFloat(m.group(2));
        }
        System.out.println(pos_x);
        System.out.println(pos_y);
    }
}
