package com.Springmvc.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
//映射类
public class MyHandler {
    private String url;
    private Object controller;
    private Method method;
}
