package com.example.workerservice.service;

public interface WorkerService {
    String pickItem(String pickerId, long itemId);
    String changePosition(String pickerId);
}
