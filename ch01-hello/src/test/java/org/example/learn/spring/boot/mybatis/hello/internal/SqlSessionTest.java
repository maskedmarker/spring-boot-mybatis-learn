package org.example.learn.spring.boot.mybatis.hello.internal;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.learn.spring.boot.mybatis.hello.dao.mapper.UserMapper;
import org.example.learn.spring.boot.mybatis.hello.model.User;
import org.example.learn.spring.boot.mybatis.hello.util.JsonUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
public class SqlSessionTest {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    SqlSession sqlSession;

    @Test
    public void test() {
        Assert.assertNotNull(sqlSessionFactory);
        Assert.assertNotNull(sqlSession);

        System.out.println("sqlSessionFactory.getClass() = " + sqlSessionFactory.getClass());
        System.out.println("sqlSession.getClass() = " + sqlSession.getClass());
    }

    @Test
    public void test0() {
        // 【关键点】每次查询 新建独立SqlSession
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> users = mapper.findAll();
            String toJsonStr = JsonUtils.toJsonStr(users);
            System.out.println("toJsonStr = " + toJsonStr);
        }
    }

    @Test
    public void test11() {
        // 【关键点】每次查询 新建独立SqlSession
        try (SqlSession session1 = sqlSessionFactory.openSession()) {
            UserMapper mapper = session1.getMapper(UserMapper.class);
            List<User> users = mapper.findAll();
            String toJsonStr = JsonUtils.toJsonStr(users);
            System.out.println("toJsonStr = " + toJsonStr);
        }

        // 第二次查询：全新SqlSession，无任何一级缓存残留
        try (SqlSession session2 = sqlSessionFactory.openSession()) {
            UserMapper mapper = session2.getMapper(UserMapper.class);
            // 强制查库，不会命中上一个session的缓存
            List<User> users2 = mapper.findAll();
            String toJsonStr = JsonUtils.toJsonStr(users2);
            System.out.println("toJsonStr = " + toJsonStr);
        }
    }

    @Test
    public void test21() {
        UserMapper mapper1 = sqlSession.getMapper(UserMapper.class);
        UserMapper mapper2 = sqlSession.getMapper(UserMapper.class);

        // 每次调用sqlSession.getMapper()生成一个新的代理对象
        Assert.assertFalse(mapper1 == mapper2);
    }

    @Test
    public void test22() {
        UserMapper mapper1 = sqlSession.getMapper(UserMapper.class);
        List<User> users1 = mapper1.findAll();
        List<User> users2 = mapper1.findAll();

        Assert.assertFalse(users1 == users2);
    }


    /**
     * DefaultSqlSession是有一级缓存的
     */
    @Test
    @Transactional
    public void test23() {
        System.out.println("sqlSession.getClass() = " + sqlSession.getClass());
        Assert.assertTrue(SqlSessionTemplate.class.isAssignableFrom(sqlSession.getClass()));

        // @Transactional开启事务后, SqlSessionTemplate.selectList()如果当前处于事务中,返回同一个DefaultSqlSession对象,DefaultSqlSession是有一级缓存的
        UserMapper mapper1 = sqlSession.getMapper(UserMapper.class);
        List<User> users1 = mapper1.findAll();
        List<User> users2 = mapper1.findAll();

        Assert.assertTrue(users1 == users2);
    }
}
