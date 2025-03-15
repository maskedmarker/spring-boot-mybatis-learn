package org.example.learn.spring.boot.mybatis.hello;

import org.apache.ibatis.ognl.MemberAccess;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlException;
import org.example.learn.spring.boot.mybatis.hello.model.User;
import org.example.learn.spring.boot.mybatis.hello.util.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mybatis的xml中的使用了ognl
 */
public class OgnlTest {

    private MemberAccess memberAccess = new MemberAccess() {
        @Override
        public Object setup(Map map, Object o, Member member, String s) {
            return null;
        }
        @Override
        public void restore(Map map, Object o, Member member, String s, Object o1) {

        }
        @Override
        public boolean isAccessible(Map map, Object o, Member member, String s) {
            int modifiers = member.getModifiers();
            return Modifier.isPublic(modifiers);
        }
    };

    /**
     * 在比较是否相等时, ognl试图先将operand转换为数值类型来比大小
     * 其中有一个是number类型的,就试图将左右operand转换为number类型
     */
    @Test
    public void test0() throws OgnlException {
        User user = new User();
        user.setName("001");
        user.setUserStatus("1");


        Map context = Ognl.createDefaultContext(user, memberAccess);
        Object value = Ognl.getValue("name == '001'", context, user);
        System.out.println("value = " + value);
        Assertions.assertTrue((Boolean) value);

        // 在解析时,'1'被识别为char
        // 左右operand都非number,同时也没有共同的parent,无法使用compareTo方法;
        // 此时盲猜是number的string表现形式,char的code-point被当作number值,string被parseDouble/parseLong
        value = Ognl.getValue("userStatus == '1'", context, user);
        System.out.println("value = " + value);
        Assertions.assertFalse((Boolean) value);

        // 在解析时,"1"被识别为string
        value = Ognl.getValue("userStatus == \"1\"", context, user);
        System.out.println("value = " + value);
        Assertions.assertTrue((Boolean) value);

        // 在解析时,1被识别为int
        // right-operand是number,所以left-operand也被当作是number的string表现形式
        value = Ognl.getValue("userStatus == 1", context, user);
        System.out.println("value = " + value);
        Assertions.assertTrue((Boolean) value);
    }
}
