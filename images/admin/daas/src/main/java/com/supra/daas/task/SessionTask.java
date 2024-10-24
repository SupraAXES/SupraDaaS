package com.supra.daas.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.supra.daas.model.SessionModel;
import com.supra.daas.repository.SessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SessionTask {

    @Autowired
    private SessionRepository sessionRepository;
    
    @Scheduled(cron = "0 0/2 * ? * ?")
    public void checkRecycle() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);

        List<SessionModel> list = sessionRepository.findAll();

        List<SessionModel> expireList = new ArrayList<>();
        for (SessionModel item : list) {
            Calendar update = Calendar.getInstance();
            update.setTime(item.getUpdateTime());
            if (calendar.after(update)) {
                expireList.add(item);
            }
        }

        if (expireList.size() > 0) sessionRepository.deleteAll(expireList);
    }

}
