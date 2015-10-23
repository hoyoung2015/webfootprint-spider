package net.hoyoung.spider.gzcy.org;

import net.hoyoung.spider.JDBCHelper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/23.
 */
public class Clean {
    public static void main(String[] args) {
        JdbcTemplate jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                "jdbc:mysql://localhost/green_org?useUnicode=true&characterEncoding=utf8",
                "root", "", 5, 30);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("select id,eval_range from gzcy_org");
        for(Map<String,Object> rs : list){
            String eval_range = (String) rs.get("eval_range");
            if(eval_range.endsWith("级")){
                eval_range += ")";
                jdbcTemplate.update("UPDATE gzcy_org SET eval_range=? WHERE id=?", eval_range, rs.get("id"));
            }else if(eval_range.endsWith(",")){
                eval_range = eval_range.substring(0,eval_range.length()-2);
                jdbcTemplate.update("UPDATE gzcy_org SET eval_range=? WHERE id=?", eval_range, rs.get("id"));
            }
            System.out.println(eval_range);
        }
        //jdbcTemplate.update("UPDATE gzcy_org SET eval_range=? WHERE id=?", eval_range, rs.get("id"));
    }
}
