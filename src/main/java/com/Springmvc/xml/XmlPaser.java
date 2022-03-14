package com.Springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.xml.parsers.SAXParser;
import java.io.InputStream;
import java.lang.annotation.Documented;

//解析类,解析XML文件中的内容
public class XmlPaser {

    /***
     * 获得路径下的所有包
     * @param path
     * @return
     */
    public static String getBasePackage(String path){
        try {
            SAXReader saxReader = new SAXReader();
            //读取xml文件
            InputStream inputstream = XmlPaser.class.getClassLoader().getResourceAsStream(path);

            Document document  = saxReader.read(inputstream);
            //获得根标签
            Element rootElement = document.getRootElement();
            //获得components-scan标签
            Element componentScan = rootElement.element("components-scan");
            //attribute就是base-package的值
            Attribute attribute = componentScan.attribute("base-package");
            //返回值
            String basePackage = attribute.getText();
            return  basePackage;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "";
    }

}
