package com.example.robotservice.event;

import com.example.robotservice.service.RobotService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@AllArgsConstructor
public class robotEventListener{
    private final RobotService robotService;

    @EventListener
    @Async
    public void getBack(RobotEvent event) throws Exception{
        robotService.turnLock();
        robotService.bitLock();
        robotService.fieldLock();
        log.info("이벤트 받음" + event.getStart().toString() + event.getShelfId().toString());
        robotService.receive(event.getStart(), event.getShelfId());
        robotService.fieldUnlock();
        robotService.bitUnlock();
        robotService.turnUnlock();
    }
}
