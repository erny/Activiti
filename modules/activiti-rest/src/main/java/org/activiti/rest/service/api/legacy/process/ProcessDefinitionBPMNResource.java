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

package org.activiti.rest.service.api.legacy.process;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Manuel Saelices
 */
public class ProcessDefinitionBPMNResource extends SecuredResource {

  @Get
  public InputRepresentation getBPMN() {
    if(authenticate() == false) return null;

    String processDefinitionId = (String) getRequest().getAttributes().get("processDefinitionId");

    if(processDefinitionId == null) {
      throw new ActivitiException("No process definition id provided");
    }

    InputStream stream = null;
    RepositoryService repository = ActivitiUtil.getRepositoryService();
    ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) ActivitiUtil.getRepositoryService())
            .getDeployedProcessDefinition(processDefinitionId);

    for (String resource: repository.getDeploymentResourceNames(pde.getDeploymentId())) {
      for (String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
        if (resource.endsWith(suffix)) {
          stream = repository.getResourceAsStream(pde.getDeploymentId(), resource);
        }
      }
    }
    if(stream == null) {
        throw new ActivitiException("Process definition " + processDefinitionId + " could not be found");
    }
    return new InputRepresentation(stream, MediaType.APPLICATION_XML);
  }
}
