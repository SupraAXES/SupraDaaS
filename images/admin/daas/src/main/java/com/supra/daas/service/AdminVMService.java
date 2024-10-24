package com.supra.daas.service;

import java.util.List;

import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.model.TaskModel;
import com.supra.daas.model.VMTemplateModel;

public interface AdminVMService {
    
    void checkTemplates();

    List<VMTemplateModel> getVMTemplates();

    void clearVm();

    boolean createResource(ResourceInfo resource);

    boolean updateResource(ResourceInfo resource, ResourceInfo oldResource);

    boolean deleteResource(ResourceInfo resource);

    boolean isRunning(ResourceInfo resource);

    boolean checkRunning(ResourceInfo resource, String unit);

    boolean checkExist(ResourceInfo resource);

    String getStatus(ResourceInfo resource, String unit);

    boolean run(ResourceInfo resource);

    boolean run(ResourceInfo resource, boolean guestVnc);

    boolean stop(ResourceInfo resource);

    boolean shutdown(ResourceInfo resource);

    boolean createVM(TaskModel model);

    boolean updateVM(TaskModel model);

    boolean runVM(TaskModel model);

    boolean stopVM(TaskModel model);

    boolean deleteVM(TaskModel model);

}
