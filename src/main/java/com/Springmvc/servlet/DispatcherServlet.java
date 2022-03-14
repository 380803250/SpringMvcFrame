package com.Springmvc.servlet;

import com.Springmvc.annotation.Controller;
import com.Springmvc.annotation.RequestMapping;
import com.Springmvc.annotation.RequestPram;
import com.Springmvc.context.WebApplicationContext;
import com.Springmvc.exception.ContextException;
import com.Springmvc.handler.MyHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private WebApplicationContext webapplicationcontext;
    //储存URI和对象方法的映射关系
    List<MyHandler> handlerList = new ArrayList<>();

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
        System.out.println("the url and method from request:"+handlerList);

    }

    //初始化请求映射
    private void initHandleMapping() {
        //判断IocMap中是否有bean对象
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
                        MyHandler handler= new MyHandler(uri, entry.getValue(), declaredMethod);
                        handlerList.add(handler);
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //进行请求的分发与处理
        executeDispatch(req, resp);
    }

    /***
     * 请求的分发处理
     * @param req
     * @param resp
     */
    private void executeDispatch(HttpServletRequest req, HttpServletResponse resp) {
        MyHandler handler = getHandler(req);
        try {
            if(handler == null){
                resp.getWriter().print("<h1>handler not found!<h1>");
            }else{
                Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
                //定义一个参数的数组
                Object[] params = new Object[parameterTypes.length];

                for(int i = 0; i<parameterTypes.length;i++){
                    Class<?> parameterType = parameterTypes[i];
                    if("HttpServletRequest".equals(parameterType.getSimpleName())){
                        params[i] = req;
                    }else if("HttpServletResponse".equals(parameterType.getSimpleName())){
                        params[i] = resp;
                    }
                }
                //获取请求中的参数集合
                Map<String, String[]> parameterMap = req.getParameterMap();
                for(Map.Entry<String, String[]> entry:parameterMap.entrySet()){
                    String name = entry.getKey();
                    String value = entry.getValue()[0];
                    System.out.println("请求参数 name:"+name+" value"+value);
                    int index = hasRequestParam(handler.getMethod(), name);
                    if(index!=-1){
                        params[index] = value;
                    }else{

                    }
                    //待优化
                    //params[2] = value;
                }
                //调用控制器中的方法
                handler.getMethod().invoke(handler.getController(), params);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取请求对应的handler
     * @param req
     * @return
     */
    public MyHandler getHandler(HttpServletRequest req){
        // /user/query
        String requestUrl = req.getRequestURI();
        //迭代映射集合
        for(MyHandler mh:handlerList){
            if(mh.getUrl().equals(requestUrl)){
                return  mh;
            }
        }
        return null;
    }

    /***
     * 判断控制器方法的参数是否有RequestPram注解,如果有点话就返回参数位置,没有找到返回-1
     * @param method
     * @param name
     * @return
     */
    public int hasRequestParam(Method method, String name){
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i<parameters.length; i++) {
            Parameter parameter = parameters[i];
            if(parameter.isAnnotationPresent(RequestPram.class)){
                RequestPram rp = parameter.getAnnotation(RequestPram.class);
                String parameterValue = rp.value();
                if(name.equals(parameterValue)){
                    return i;
                }
            }
        }
        return -1;
    }
}
