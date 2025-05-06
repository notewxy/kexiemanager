package com.wan.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SSFactory {
    private static SqlSessionFactory sqlSessionFactory;
    private static final String file = "mybatis.xml";

    static {
        try {
            InputStream is = Resources.getResourceAsStream(file);
            if (is == null){
                throw new RuntimeException("找不到Mybatis的配置文件："+file);
            }
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
            System.out.println("SSF成功创建");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getSSF(){
        return sqlSessionFactory;
    }
}












