package com.bruce.service.imple;

import com.Springmvc.annotation.Service;
import com.bruce.pojo.User;
import com.bruce.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service//没加参数则默认为类的首字母小写
public class UserServiceImpl implements UserService {

    @Override
    public List<User> findUser(String name) {
        System.out.println("调用服务层UserServiceImp中的findUser方法");
        List<User> list = new ArrayList<>();
        list.add(new User("380803250","daChen", 1));
        list.add(new User("admin","123", 2));
        return list;
    }

    @Override
    public String getUserMessage(String name) {
        return "调用服务层UserServiceImp中的getUserMessage方法, 参数值为"+name;
    }
}
