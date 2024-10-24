package com.supra.daas.component;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.supra.daas.task.Task;

@Component
public class ApplicationInit implements ApplicationRunner {

    @Autowired
    private Task task;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        task.resetStatus();
        task.checkTemplateTask();
    }

}
