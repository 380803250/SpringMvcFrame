package com.bruce.test;

import com.Springmvc.xml.XmlPaser;
import org.junit.Test;

public class TestSpringMvc  {
    @Test
    public void testreadxml(){
        String basePackage = XmlPaser.getBasePackage(("springmvc.xml"));
        System.out.print(basePackage);
    }
}
