package io.github.jasonlat.trigger.http;

import io.github.jasonlat.middleware.annotations.decrypt.RequestDecryption;
import io.github.jasonlat.middleware.annotations.encrypt.RequestEncryption;
import io.github.jasonlat.types.model.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/test/")
@CrossOrigin(value = {"*"})
public class TestEccController {

    @PostMapping("/1")
    public Response<String> test1() {
        return Response.ok("无需权限校验和加密解密的接口（不带token）");
    }

    @RequestEncryption()
    @PostMapping("/2")
    public Response<String> test2() {
        // 在 SecurityConfig 中配置白名单, 即使带了token也会跳过认证
        return Response.ok("在jwt白名单接口（带token）");
    }

    @RequestEncryption()
    @PostMapping("/3")
    public Response<String> test3() {
        // 不在 SecurityConfig 中白名单, 要进行认证
        return Response.ok("不在jwt白名单接口（带token）");
    }

    @RequestDecryption()
    @RequestEncryption()
    @GetMapping("/4")
    public Response<String> test4(@RequestParam(value = "params", required = false) String params) {
        // 不在 SecurityConfig 中白名单, 要进行认证
        return Response.ok("GET请求，不在jwt白名单接口（带token）,参数：" + params);
    }


}
