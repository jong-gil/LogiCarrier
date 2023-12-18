package com.example.workerservice.Controller;

import com.example.workerservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
public class WorkerController {
    private final RedisService redisService;
    private final Environment env;

    @GetMapping("/health_check")
    public String status(HttpServletRequest request) {
        log.info("Server port={}", request.getServerPort());
        return String.format("It's working in Worker Service on PORT %s"
                , env.getProperty("local.server.port"));
    }

    @GetMapping("/workerBit")
    public String getWorkerBit() {
        return redisService.getBit("workerBit");
    }

    @GetMapping("/progressBit")
    public String getProgressBit() {
        return redisService.getBit("progressBit");
    }

    @PostMapping("/setBits")
    public void setInitialBits() {
        redisService.setInitialWorkerBit();
        redisService.setInitialProgressBit();
    }

    @PostMapping("/workerBit/{positionNum}/{userType}")
    public String setWorkerBit(@PathVariable("positionNum") int positionNum, @PathVariable("userType") String userType) {
        return redisService.setWorkerBit(positionNum, userType);
    }

    @PostMapping("/progressBit/{workerId}")
    public String setProgressBit(@PathVariable("workerId") Long id) {
        return redisService.setProgressBit(id);
    }
}
