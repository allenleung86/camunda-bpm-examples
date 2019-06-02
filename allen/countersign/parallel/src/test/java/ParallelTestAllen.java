import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

/**
 * 会签并行测试
 *
 * @author Stefan Hentschel.
 */
public class ParallelTestAllen {
    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    /**
     * 多实例任务（并行）流程部署
     *
     * @param
     * @return void
     * @author liangzhaolin
     * @date 2019/6/2 16:56
     */
    @Test
    @Deployment(resources = {"countersign_parallel_allen.bpmn"})
    public void countersignParallel() {
        RuntimeService runtimeService = processEngineRule.getRuntimeService();
        TaskService taskService = processEngineRule.getTaskService();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("countersign_parallel_task_allen");
        String processInstanceId = processInstance.getProcessInstanceId();

        int taskNums = 0;

        // 对于并行的一次能取全部task处理
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (Task task: taskList) {
            String taskId = task.getId();
            taskService.complete(taskId);

            taskNums ++;
        }

        System.out.println("the end, 一共并行执行了 " + taskNums + " 个任务.");
    }

}
