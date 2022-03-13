package com.Springmvc.servlet;

import com.Springmvc.annotation.Autowired;
import com.Springmvc.annotation.Controller;
import com.Springmvc.annotation.RequestMapping;
import com.Springmvc.context.WebApplicationContext;
import com.Springmvc.exception.ContextException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private WebApplicationContext webapplicationcontext;

    @Override
    public void init() throws ServletException {
        //1.SERVLET初始化的时候读取初始化参数,拿到classpath:springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");
        //2.创建SPRING容器
        webapplicationcontext = new WebApplicationContext(contextConfigLocation );
        //3.初始化web容器
        webapplicationcontext.refresh();
        //4.初始化请求映射 /user/query --->?Controller --->?Method --->parameter
        initHandleMapping();

    }

    //初始化请求映射
    private void initHandleMapping() {
        //判断Iocmap中是否有bean对象
        if(webapplicationcontext.iocMap.isEmpty()){
            throw new ContextException("Spring Context is null!!");
        }
        for(Map.Entry<String, Object> entry:webapplicationcontext.iocMap.entrySet()){
            Class<?> aClass = entry.getValue().getClass();
            if(aClass.isAnnotationPresent(Controller.class)){
                Method[] declaredMethods = aClass.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if(declaredMethod.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping RequestMappingAnnotation = declaredMethod.getAnnotation(RequestMapping.class);
                        // /user/query
                        String uri = RequestMappingAnnotation.value();
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
