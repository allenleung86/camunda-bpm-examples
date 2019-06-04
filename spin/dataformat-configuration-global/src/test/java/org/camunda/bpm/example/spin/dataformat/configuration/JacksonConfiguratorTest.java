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
package org.camunda.bpm.example.spin.dataformat.configuration;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder;
import org.camunda.spin.DataFormats;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class JacksonConfiguratorTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources={"testProcessAllen.bpmn"})
//  @Deployment(resources={"testProcess.bpmn"})
  public void shouldSerializeJsonCorrectly() {
    RuntimeService runtimeService = processEngineRule.getRuntimeService();

    Car car = new Car();
    car.setPrice(new Money(999));
//    car.setPrice(new Money(1000));

    ObjectValueBuilder objectValueBuilder = Variables.objectValue(car);
    objectValueBuilder = objectValueBuilder.serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME);
    ObjectValue objectValue = objectValueBuilder.create();

    VariableMap carMap = Variables.createVariables().putValueTyped("car", objectValue);

    // request that car is serialized as JSON when stored 请求在存储时将car序列化为JSON
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", carMap);

    // access the serialized JSON value 访问序列化的JSON值
    SerializableValue serializedCarValue = runtimeService.getVariableTyped(processInstance.getId(), "car");
    String carJson = serializedCarValue.getValueSerialized();
    Assert.assertEquals("{\"price\":999}", carJson);
//    Assert.assertEquals("{\"price\":1000}", carJson);

    // assert that the script task was able to extract the price property from the JSON object
    Number price = (Number) runtimeService.getVariable(processInstance.getId(), "price");
    Assert.assertEquals(999, price);
//    Assert.assertEquals(1000, price);
  }


}
