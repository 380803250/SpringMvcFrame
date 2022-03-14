package com.Springmvc.servlet;

import com.Springmvc.annotation.Controller;
import com.Springmvc.annotation.RequestMapping;
import com.Springmvc.annotation.RequestPram;
import com.Springmvc.annotation.ResponseBody;
import com.Springmvc.context.WebApplicationContext;
import com.Springmvc.exception.ContextException;
import com.Springmvc.handler.MyHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //进行请求的分发与处理
        executeDispatch(req, resp);
    }

    @Override
    public void init() throws ServletException {
        //1.SERVLET初始化的时候读取初始化参数,拿到classpath:springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");

        //2.创建SPRING容器
        webapplicationcontext = new WebApplicationContext(contextConfigLocation );

        //3.初始化web容器
        webapplicationcontext.refresh();
        System.out.println("After webContext.refresh");

        //4.初始化请求映射 /user/query --->?Controller --->?Method --->parameter
        initHandleMapping();
        System.out.println("After initHandleMapping");
        System.out.println("the url and method from request:"+handlerList);

    }

    /***
     * 初始化请求映射
     */
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
                    System.out.println("请求参数 name:"+name+" value:"+value);
                    int index = hasRequestParam(handler.getMethod(), name);
                    if(index!=-1){
                        params[index] = value;
                    }else{
                        List<String> names = getParameterNames(handler.getMethod());
                        System.out.println(names);
                        for(int i = 0; i<names.size(); i++){
                            //判断是否参数名字与容器中名字匹配
                            if(name.equals((names.get(i)))){
                                params[i] = value;
                                break;
                            }
                        }
                    }
                    //未优化前的输出赋值,只能依靠参数位置约束来确定
                    //params[2] = value;
                }
                //调用控制器中的方法(void方法)
                //handler.getMethod().invoke(handler.getController(), params);

                //调用控制器中的方法(String方法)
                Object result = handler.getMethod().invoke(handler.getController(), params);
                //controller中String类型方法转发到user.jsp
                if(result instanceof String){
                    String viewName = (String)result;
                    //包含则为转发规则,forward
                    if(viewName.contains(":")){
                        String viewType = viewName.split(":")[0];
                        String viewPage = viewName.split(":")[1];
                        if(viewType.equals("forward")){
                            req.getRequestDispatcher(viewPage).forward(req, resp);
                        }else{
                            //redirect
                            resp.sendRedirect(viewPage);
                        }
                    }else{
                        //默认转发
                        req.getRequestDispatcher(viewName).forward(req, resp);
                    }
                }else{
                    //返回的是Json数据
                    Method method = handler.getMethod();
                    if(method.isAnnotationPresent(ResponseBody.class)){
                        //把返回值调用转为Json的数据转为Json字符串
                        ObjectMapper objectMapper = new ObjectMapper();
                        String json = objectMapper.writeValueAsString(result);
                        resp.setContentType("text/html; charset=utf-8");
                        PrintWriter writer = resp.getWriter();
                        writer.print(json);
                        writer.flush();
                        writer.close();
                    }
                }

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

    /***
     * 将method中的参数名字拿到
     * @param method
     * @return
     */
    public List<String> getParameterNames (Method method){
        List<String> list = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            list.add(parameter.getName());
        }
        return list;
    }
}
