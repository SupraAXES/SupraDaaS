package com.supra.daas.task;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.supra.daas.model.TaskModel;
import com.supra.daas.repository.TaskRepository;
import com.supra.daas.service.AdminVMService;
import com.supra.daas.util.EmptyUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Task {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AdminVMService adminVMService;

    private ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(
        5, 20, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    @Scheduled(cron = "0/2 * * ? * ?")
    public void checkRunTask() {
        List<TaskModel> models = taskRepository.findAll();
        if (EmptyUtils.isEmpty(models)) {
            return;
        }

        Calendar yestoday = Calendar.getInstance();
        yestoday.add(Calendar.HOUR, -2);

        for (TaskModel item : models) {
            if (item.getStatus().intValue() > 0) {
                continue;
            }

            if ("VM".equals(item.getCategory())) {
                if (yestoday.getTime().after(item.getCreateTime())
                    || item.getStatus() < -2) {
                    taskRepository.delete(item);
                    continue;
                }
                
                log.info("excute task: " + JSON.toJSONString(item));

                item.setStatus(-1 * item.getStatus() + 1);
                taskRepository.save(item);

                taskExecutor.execute(() -> {
                    if (executeVmTask(item)) {
                        log.info("excute task success");
                        taskRepository.delete(item);
                    } else {
                        log.info("excute task failed （" + item.getStatus() + "/3）");
                        item.setStatus(-1 * item.getStatus());
                        taskRepository.save(item);
                    }
                });
                
            }
        }
    }

    @Scheduled(cron = "0 0/5 * ? * ?")
    public void checkTemplateTask() {
        adminVMService.checkTemplates();
    }

    private boolean executeVmTask(TaskModel model) {
        boolean result = false;
        if ("create_vm".equals(model.getAction())) {
            result = adminVMService.createVM(model);
        } else if ("update_vm".equals(model.getAction())) {
            result = adminVMService.updateVM(model);
        } else if ("run_vm".equals(model.getAction())) {
            result = adminVMService.runVM(model);
        } else if ("stop_vm".equals(model.getAction())) {
            result = adminVMService.stopVM(model);
        } else if ("delete_vm".equals(model.getAction())) {
            result = adminVMService.deleteVM(model);
        }

        return result;
    }

    public void resetStatus() {
        taskRepository.findAll().forEach(task -> {
            task.setStatus(0);
            taskRepository.save(task);
        });
    }

}
