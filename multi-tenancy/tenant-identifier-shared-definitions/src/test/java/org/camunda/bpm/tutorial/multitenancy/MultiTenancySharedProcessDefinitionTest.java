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
package org.camunda.bpm.tutorial.multitenancy;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MultiTenancySharedProcessDefinitionTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private TaskService taskService;
  private IdentityService identityService;

  @Before
  public void initServices() {
    repositoryService = processEngineRule.getRepositoryService();
    runtimeService = processEngineRule.getRuntimeService();
    taskService = processEngineRule.getTaskService();
    identityService = processEngineRule.getIdentityService();
  }

  /**
   * 为共享流程定义的实例提供租户id
   *
   * <p>You will learn how to provide tenant ids for instances of shared process definitions 您将了解如何为共享流程定义的实例提供租户id</p>
   * <p>To verify the behavior you have to:</p>
   * <p>1.deploy the process definitions without a tenant id 在没有租户id的情况下部署流程定义</p>
   * <p>2.set the authenticated tenant 设置经过身份验证的租户</p>
   * <p>3.create an instance of a shared process definition 创建共享流程定义的实例</p>
   *
   * @param
   * @return void
   * @author liangzhaolin
   * @date 2019/6/3 14:55
   */
  @Test
  public void testTenantIdProvider() {
    // deploy shared process definitions (which belongs to no tenant)
    repositoryService
      .createDeployment()
            .name("为共享流程定义的实例提供租户id")
            .source("本地测试")
      .addClasspathResource("processes/default/mainProcess.bpmn")
      .addClasspathResource("processes/default/subProcess.bpmn")
      .deploy();

    // set the authenticated tenant and start a process instance
    /*
     * 传递此线程的经过身份验证的用户id、组id和租户id
     * 同一线程执行的所有服务方法调用都可以访问此身份验证
     * 在交互终止后会调用“clearAuthentication”
     */
    identityService.setAuthentication("john123", null, Collections.singletonList("tenant1"));

    ProcessInstance mainProcessInstance = runtimeService.startProcessInstanceByKey("mainProcess");
    String processInstanceId = mainProcessInstance.getProcessInstanceId();

    // check that the process instance got the tenant id from the custom tenant id provider
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("mainProcess").processInstanceId(processInstanceId).singleResult();
//    assertThat(processInstance.getTenantId(), is("tenant1"));

    // and started the default sub-process
    Task task = taskService.createTaskQuery().processDefinitionKey("subProcess").singleResult();
//    assertThat(task.getName(), is("task in default subprocess"));
  }

  /**
   * 测试覆盖共享流程定义的情况
   *
   * <p>You will learn how to override default process definitions with tenant specific once 您将了解如何使用特定于租户的方法一次覆盖缺省流程定义</p>
   * <p>To verify the behavior you have to </p>
   * <p>deploy the default process definitions without a tenant id 部署没有租户id的默认流程定义</p>
   * <p>deploy the sub-process definition for tenant ('tenant2') 为租户tenant2部署子流程定义</p>
   * <p>set the authenticated tenant 设置经过身份验证的租户</p>
   * <p>create an instance of a shared process definition 创建共享流程定义的实例</p>
   * <p>and checks if it calls the default or the tenant specific sub-process 检查它是否调用默认的子进程或租户特定的子进程</p>
   *
   * @param
   * @return void
   * @author liangzhaolin
   * @date 2019/6/3 14:54
   */
  @Test
  public void testOverrideSharedProcessDefinition() {
    // deploy default process definitions (which belongs to no tenant)
    repositoryService
      .createDeployment()
      .addClasspathResource("processes/default/mainProcess.bpmn")
      .addClasspathResource("processes/default/subProcess.bpmn")
            .name("使用指定租户覆盖缺省流程定义")
            .source("本地测试1")
      .deploy();

    // deploy custom process definition for 'tenant2'
    repositoryService
      .createDeployment()
      .tenantId("tenant2")
      .addClasspathResource("processes/tenant2/subProcess.bpmn")
            .name("部署tenant2租户的子流程")
            .source("本地测试2")
      .deploy();

    // set the authenticated tenant and start a process instance
//    identityService.setAuthentication("mary123", null, Collections.singletonList("tenant1"));
    identityService.setAuthentication("mary", null, Collections.singletonList("tenant2"));

    runtimeService.startProcessInstanceByKey("mainProcess");

    // check that the process instance has the tenant id 'tenant2'
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("mainProcess").singleResult();
    assertThat(processInstance.getTenantId(), is("tenant2"));

    // and started the tenant-specific sub-process which overrides the default one
    Task task = taskService.createTaskQuery().processDefinitionKey("subProcess").singleResult();
    assertThat(task.getName(), is("task in tenant specific subprocess"));
  }

  /**
   * 单元测试后,清除所有部署信息
   *
   * <p>删除给定的部署和对流程实例、历史流程实例和作业的级联删除。</p>
   *
   * @param
   * @return void
   * @author liangzhaolin
   * @date 2019/6/3 17:25
   */
  @After
  public void clean() {
    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
