
package io.github.jasonlat;

import io.github.jasonlat.middleware.domain.service.EccUserDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@Configurable
@EnableScheduling
@SpringBootApplication
@ComponentScan(value = {"io.github.wppli", "io.github.jasonlat"}) // 扫描指定包下的组件
public class Application implements CommandLineRunner {

    private final ApplicationContext context;
    public Application(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args){
        SpringApplication.run(Application.class);
        log.info("项目开始启动...");
    }
    @Override
    public void run(String... args) throws Exception {
        EccUserDataService service = context.getBean(EccUserDataService.class);
        System.out.println("实际加载的Bean类型：" + service.getClass().getName());

        // 获取所有EccUserDataService类型的Bean名称
        String[] beanNames = context.getBeanNamesForType(EccUserDataService.class);
        System.out.println("EccUserDataService类型的Bean数量：" + beanNames.length);
        for (String name : beanNames) {
            EccUserDataService service2 = context.getBean(name, EccUserDataService.class);
            System.out.println("Bean名称：" + name + "，类型：" + service2.getClass().getName());
        }
    }
}
