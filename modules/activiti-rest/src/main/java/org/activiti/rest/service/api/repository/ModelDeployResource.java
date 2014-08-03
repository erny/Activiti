/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.repository;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.workflow.simple.converter.json.SimpleWorkflowJsonConverter;

import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Manuel Saelices
 */
public class ModelDeployResource extends BaseModelResource {

  private static final Logger logger = LoggerFactory.getLogger(ModelDeployResource.class);

  @Get
  public ModelDeployResourceResponse deployModel() {
    if(authenticate() == false) return null;

    Model model = getModelFromRequest();
    SimpleWorkflowJsonConverter jsonConverter = new SimpleWorkflowJsonConverter();
    RepositoryService repositoryService = ActivitiUtil.getRepositoryService();
    try {
      byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
      ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(editorSource);
      BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
      byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);

      String processName = model.getName() + ".bpmn20.xml";
      Deployment deployment = repositoryService.createDeployment()
              .name(model.getName())
              .addString(processName, new String(bpmnBytes))
              .deploy();

      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
              .deploymentId(deployment.getId()).singleResult();
      return new ModelDeployResourceResponse(processDefinition.getId());
    } catch(Exception e) {
      logger.error("failed to export model to BPMN XML", e);
      throw new ActivitiException("Error exporting model to BPMN XML: " + e.getMessage(), e);
    }
  }

  static class ModelDeployResourceResponse {

    protected String processDefinitionId;

    public ModelDeployResourceResponse(String processDefinitionid) {
      this.processDefinitionId = processDefinitionid;
    }

    public String getProcessDefinitionId() {
      return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }
  }
}
