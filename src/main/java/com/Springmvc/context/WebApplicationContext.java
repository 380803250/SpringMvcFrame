package com.Springmvc.context;

import com.Springmvc.annotation.Autowired;
import com.Springmvc.annotation.Controller;
import com.Springmvc.annotation.Service;
import com.Springmvc.exception.ContextException;
import com.Springmvc.xml.XmlPaser;

import javax.print.DocFlavor;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.Iterable;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Spring容器类
public class WebApplicationContext {
    //得到springmvc.xml
    String contextConfigLocation;

    List<String> classNameList = new ArrayList<String>();

    //spring的IOC容器
    public Map<String, Object> iocMap = new ConcurrentHashMap<String, Object>();

    public WebApplicationContext(String string) {
        this.contextConfigLocation = string;
    }
    //初始化容器
    public void refresh(){
        //通过路径拿到配置文件名
        String basePackage = XmlPaser.getBasePackage(contextConfigLocation.split(":")[1]);

        //com.bruce.service
        //com.bruce.controller
        String[] basePackages = basePackage.split(",");
        if(basePackages.length>0){
            //遍历包中的类并添加进classNameList
            for(String pac :basePackages){
                executeScanPackage(pac);
            }
            System.out.println("The context in SpringMVC is:"+classNameList);
            //实例化Spring容器中的BEAN
            executionBean();
            //Ioc容器当中的对象是
            System.out.println("The Bean in Spring is:"+iocMap);
            //注入操作
            executeAutowired();
        }
    }

    /***
     * 实现spring容器中对象的属性依赖注入, *CLASS类中declaredField方法
     */
    private void executeAutowired() {
        try {
            if(iocMap.isEmpty()){
                throw new ContextException("can't find bean!");
            }
            for(Map.Entry<String, Object> entry : iocMap.entrySet()){
                String key = entry.getKey();
                Object bean = entry.getValue();
                //得到Bean中所有的字段
                Field[] declaredFields = bean.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if(declaredField.isAnnotationPresent(Autowired.class)){
                        Autowired AutowiredAnnotation = declaredField.getAnnotation(Autowired.class);
                        String beanName = AutowiredAnnotation.value();
                        if("".equals(beanName)){
                            Class<?> type = declaredField.getType();
                            beanName = type.getSimpleName().substring(0,1).toLowerCase()+type.getSimpleName().substring(1);
                        }
                        declaredField.setAccessible(true);
                        //属性注入 使用反射给属性赋值
                        declaredField.set(bean, iocMap.get(beanName));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /***
     * 通过反射机制实例化Spring容器中的实例化对象,并且加入iocMap<类名,实例化对象>
     */
    private void executionBean() {
        if(classNameList ==null){
            throw new ContextException("没有扫描到的实例化对象");
        }
        for(String className:classNameList){
            try {
                Class clazz = Class.forName(className);
                //isAnnotationPresent判断是否有注解
                if(clazz.isAnnotationPresent(Controller.class)){
                    //控制层类com.bruce.controller
                    //得到控制层的类名, .substring(0,1)将第一个字母转成小写UserController->userController
                    String beanName = clazz.getSimpleName().substring(0,1).toLowerCase()+clazz.getSimpleName().substring(1);
                    iocMap.put(beanName, clazz.newInstance());
                }else if(clazz.isAnnotationPresent(Service.class)){
                    //业务层类com.bruce.service.impl.userServiceImpl
                    Service serviceAnnotation = (Service) clazz.getAnnotation(Service.class);
                    String beanName = serviceAnnotation.value();
                    if("".equals(beanName)){
                        Class<?>[] interfaces = clazz.getInterfaces();
                        for(Class<?> c1 : interfaces){
                            //处理后{userController=com.bruce.controller.UserController@16eee332, userService=com.bruce.service.imple.UserServiceImpl@1114eadc}
                            //处理前{userController=com.bruce.controller.UserController@70c29666, com.bruce.service.UserService=com.bruce.service.imple.UserServiceImpl@4d46ce33}
                            String beanName1 = c1.getSimpleName().substring(0,1).toLowerCase()+c1.getSimpleName().substring(1);
                            iocMap.put(beanName1, clazz.newInstance());
                        }
                    }else{
                        iocMap.put(beanName, clazz.newInstance());
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 扫描目录下包中的类,并添加进classNameList(保存类名的集合)
     * @param pac
     */
    public void executeScanPackage(String pac){
        //com.bruce.service
        URL url = this.getClass().getClassLoader().getResource("/"+pac.replaceAll("\\.", "/"));
        String path = url.getFile();
        File dir = new File(path);
        for(File f: dir.listFiles()){
            if(f.isDirectory()){
                //是文件目录
                executeScanPackage(pac+"."+f.getName());
            }else{
                //是目录下的文件
                String className = pac+"."+f.getName().replaceAll(".class", "");
                classNameList.add(className);
            }
        }
    }
}
