package com.bruce.test;

import com.Springmvc.servlet.DispatcherServlet;
import com.Springmvc.xml.XmlPaser;
import org.junit.Test;

import javax.servlet.ServletException;

public class TestWebApplicationContext {
    @Test
    public void test(){
        try {
            DispatcherServlet ds = new DispatcherServlet();
            ds.init();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
