package org.example.learn.spring.boot.mybatis.hello.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.learn.spring.boot.mybatis.hello.model.User;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<User> findAll();

    int save(User user);

    List<User> queryByParam(Map<String, Object> param);

    List<User> queryByParam2(Map<String, Object> param);

    List<User> queryByParam3(Map<String, Object> param);
}
