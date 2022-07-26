package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Generated;
import java.util.Date;

/**
 * @Author liushizhong
 * @Date 2022/3/4 21:55
 * @Version 1.0
 */
@Controller
public class DataController {

    @Autowired
    private DataService dataService;


    /**
     * 跳转统计页面
     * @return
     */
    @RequestMapping(value = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    /**
     * 统计网站UV
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyy-MM-dd")Date end, Model model){


        long uv = dataService.calculateUV(start,end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        // 只处理一半，又发到当前页
        return "forward:/data";
    }

    /**
     * 统计活跃用户
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyy-MM-dd")Date end, Model model){


        long dau = dataService.calculateDAU(start,end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return "forward:/data";
    }
}
