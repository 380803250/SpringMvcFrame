package com.bruce.controller;

import com.Springmvc.annotation.Autowired;
import com.Springmvc.annotation.Controller;
import com.Springmvc.annotation.RequestMapping;
import com.Springmvc.annotation.RequestPram;
import com.bruce.pojo.User;
import com.bruce.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller//默认首字母转为小写
public class UserController {
    @Autowired
    UserService userservice;

    @RequestMapping("/user/query")
    public void findUser(HttpServletRequest req, HttpServletResponse res,@RequestPram("name") String name){
        //处理响应中文代码问题
        res.setContentType("txt/html; charset=utf-8");
        try {
            List<User> list = userservice.findUser(name);
            PrintWriter pw = res.getWriter();
            pw.print("<h1>SpringMvc控制器"+name+"<h1>");
            pw.print("SpringMvc done"+name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
