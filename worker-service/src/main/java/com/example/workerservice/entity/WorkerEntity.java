package com.example.workerservice.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class WorkerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workerId;

    private boolean status; // true: pusher, false: picker

    private boolean progress; // true: in-progress, false: not in progress

    private Long userId;

    public void changeStatus(boolean currentStatus) {
        this.status = !currentStatus;
    }

}
