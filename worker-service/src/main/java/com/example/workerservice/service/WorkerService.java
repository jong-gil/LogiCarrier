package com.example.workerservice.service;

public interface WorkerService {
    String pickItem(String pickerId, long itemId) throws Exception;
    String changePosition(String pickerId);
}
