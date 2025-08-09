package io.github.jasonlat.infrastructure.persistent.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoGenerator {

    // 数据库连接信息
    private static final String URL = "jdbc:mysql://192.168.3.16:13306/ecc?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "lijiaqiang12@";

    private static final String outputDir =
            System.getProperty("user.dir") + "\\" + "src\\main\\java";

    private static final String parentPacket = "io.github.jasonlat.infrastructure.persistent";

    private static final String mapperXmlPath =
            System.getProperty("user.dir") + "\\" + "src\\main\\resources\\mybatis\\mapper";

    public static void main(String[] args) {
        // 需要生成代码的表
        List<String> tables = new ArrayList<>();
        tables.add("user_key");


        // 配置全局设置
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> {
                    builder.author("lijiaqiang@ljq1024.cc")               // 作者名称
                            .outputDir(outputDir)    // 输出目录路径（到java目录）
                            .commentDate("yyyy-MM-dd")  // 注释日期格式
                            .fileOverride();            // 启用文件覆盖以前生成的文件

                })
                // 配置包结构
                .packageConfig(builder -> {
                    builder.parent(parentPacket)  // 父包名称
                            .entity("po")
                            .mapper("dao")
                            .xml("mybatis/mapper").pathInfo(Collections.singletonMap(OutputFile.xml, mapperXmlPath));  // XML文件的输出目录路径
                })
                // 配置代码生成策略
                .strategyConfig(builder -> {
                    builder.addInclude(tables)
//                            .addTablePrefix("p_") // 指定数据库表名的前缀，生成代码时会自动忽略前缀部分，只生成以指定前缀后面的部分为表名的代码文件
                            .entityBuilder()
                            .formatFileName("%s")
                            .enableLombok()
                            .enableTableFieldAnnotation()  // 启用表字段注释（例如@TableField）

                            .mapperBuilder()
                            .enableBaseResultMap()  // 为所有表启用一个通用的resultMap生成
                            .formatMapperFileName("I%sDao")  // Mapper文件名格式（例如SysUserMapper）
                            .enableMapperAnnotation()  // 启用Mapper注释（例如@Mapper）
                            .formatXmlFileName("%sMapper");  // XML文件名格式（例如SysUserMapper.xml）
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker模板引擎（默认为Velocity）
                .execute();
    }

}

