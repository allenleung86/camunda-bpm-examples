/*
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
package org.camunda.bpm.example.servicetask.rest;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * 测试传入的日期是否为节假日
 *
 * @author Stefan Hentschel.
 */
public class ServiceTaskRestTestAllen {
  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources={"invokeRestServiceAllen.bpmn"})
  public void shouldPackForHoliday() {

    // 构造入参
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("allenparam1", "allenvalue1");
    variables.put("allenparam2", "allenvalue2");
    variables.put("allenparam3", "allenvalue3");

    RuntimeService runtimeService = processEngineRule.getRuntimeService();
    TaskService taskService = processEngineRule.getTaskService();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayallen", variables);
    String processInstanceId = processInstance.getProcessInstanceId();

    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    Assert.assertNotNull(task);
    assertEquals("Pack for holiday", task.getName());

    boolean isHoliday = Boolean.parseBoolean(taskService.getVariable(task.getId(), "isHoliday").toString());
    Assert.assertTrue(isHoliday);
  }

  @Test
  @Deployment(resources={"invokeRestServiceAllen.bpmn"})
  public void shouldPackForWork() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("date", "2014-01-02");

    RuntimeService runtimeService = processEngineRule.getRuntimeService();
    TaskService taskService = processEngineRule.getTaskService();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayallen", variables);

    String processInstanceId = processInstance.getProcessInstanceId();

    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    Assert.assertNotNull(task);
    assertEquals("Pack for work", task.getName());

    boolean isHoliday = Boolean.parseBoolean(taskService.getVariable(task.getId(), "isHoliday").toString());
    Assert.assertFalse(isHoliday);
  }

}
