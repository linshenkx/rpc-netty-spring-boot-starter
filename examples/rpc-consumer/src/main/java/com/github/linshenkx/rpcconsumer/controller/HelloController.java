package com.github.linshenkx.rpcconsumer.controller;


import com.github.linshenkx.rpclib.HelloService;
import com.github.linshenkx.rpcnettycommon.annotation.RpcReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@RestController
@Log4j2
public class HelloController {

    @RpcReference
    private HelloService helloService;

    @GetMapping("/hello")
    public String sayHello(@RequestParam(defaultValue = "lin") String name){
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        String returnString= helloService.say(name);
        stopwatch.stop();
        log.info("耗时："+stopwatch.getTotalTimeSeconds()+"seconds");
        return returnString;
    }

}
