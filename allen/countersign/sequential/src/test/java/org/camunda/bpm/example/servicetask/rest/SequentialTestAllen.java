package org.camunda.bpm.example.servicetask.rest;/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.lang3.ObjectUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * 会签串行测试
 */
public class SequentialTestAllen {
    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    /**
     * 多实例任务（串行）流程
     *
     * @param
     * @return void
     * @author liangzhaolin
     * @date 2019/6/2 16:56
     */
    @Test
    @Deployment(resources = {"countersign_sequential_allen.bpmn"})
    public void countersignSequential() {
        RuntimeService runtimeService = processEngineRule.getRuntimeService();
        TaskService taskService = processEngineRule.getTaskService();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("countersign_sequential_task_allen");
        String processInstanceId = processInstance.getProcessInstanceId();

        int taskNums = 0;

        // 对于串行的一次只能取一个
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        while (!ObjectUtils.isEmpty(task)) { // 如果task不为空则继续逐个complete处理
            String taskId = task.getId();
            taskService.complete(taskId);
            task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();

            taskNums ++;
        }

        System.out.println("the end, 一共串行执行了 " + taskNums + " 个任务.");
    }

}
