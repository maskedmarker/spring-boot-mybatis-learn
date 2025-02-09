package org.example.learn.spring.boot.mybatis.hello.config;

import org.example.learn.spring.boot.mybatis.hello.mapper.MybatisScanFlag;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan(basePackageClasses = {MybatisScanFlag.class})
//@MapperScan(basePackages={"org.example.learn.spring.boot.mybatis.hello.mapper"})
@Configuration
public class MybatisConfig {
}
