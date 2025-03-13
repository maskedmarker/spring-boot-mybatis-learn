package org.example.learn.spring.boot.mybatis.hello;

import org.example.learn.spring.boot.mybatis.hello.dao.mapper.UserMapper;
import org.example.learn.spring.boot.mybatis.hello.model.User;
import org.example.learn.spring.boot.mybatis.hello.service.UserService;
import org.example.learn.spring.boot.mybatis.hello.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test0() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", null);
        param.put("name", "001");
        param.put("email", null);
        List<User> users = userMapper.queryByParam(param);
        String toJsonStr = JsonUtils.toJsonStr(users);
        System.out.println("toJsonStr = " + toJsonStr);
    }
}
