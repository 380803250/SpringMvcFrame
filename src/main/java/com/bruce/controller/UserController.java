package com.bruce.controller;

import com.Springmvc.annotation.*;
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
    public String findUser(HttpServletRequest req, HttpServletResponse res,/*@RequestPram("username")*/ String name){
        //void方法
        //处理响应中文代码问题
//        res.setContentType("text/html; charset=utf-8");
//        try {
//            List<User> list = userservice.findUser(name);
//            PrintWriter out = res.getWriter();
//            out.print("<h1>SpringMvc控制器:"+name+"<h1>");
//            out.print("SpringMvc done"+name);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //String方法,重新定向至user.jsp
        res.setContentType("text/html; charset=utf-8");
        String userMessage = userservice.getUserMessage(name);
        req.setAttribute("userMessage", userMessage);
        return "forward:/user.jsp";
    }

    /***
     * 返回一个集合给dispatcher
     * @param req
     * @param res
     * @param name
     * @return
     */
    @RequestMapping("/user/queryjson")
    //返回值自动转为Json
    @ResponseBody
    public List<User> queryUsers(HttpServletRequest req, HttpServletResponse res,/*@RequestPram("username")*/ String name){
        return userservice.findUser(name);
    }
}
