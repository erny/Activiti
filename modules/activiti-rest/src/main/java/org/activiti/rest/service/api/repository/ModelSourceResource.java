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

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.Model;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Frederik Heremans
 */
public class ModelSourceResource extends BaseModelSourceResource {

  @Override
  protected InputRepresentation getModelStream(Model model) {
    byte[] editorSource = ActivitiUtil.getRepositoryService().getModelEditorSource(model.getId());
    if(editorSource == null) {
      throw new ActivitiObjectNotFoundException("Model with id '" + model.getId() + "' does not have source available.", String.class);
    }
    return new InputRepresentation(new ByteArrayInputStream(editorSource), MediaType.APPLICATION_OCTET_STREAM);
  }
  
  @Override
  protected void setModelSource(Model model, byte[] byteArray) {
    XMLInputFactory xif = XMLInputFactory.newInstance();
    byte[] modelBytes = null;
    try {
      XMLStreamReader xtr = xif.createXMLStreamReader(new ByteArrayInputStream(byteArray));
      BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
      BpmnJsonConverter converter = new BpmnJsonConverter();
      ObjectNode modelNode = converter.convertToJson(bpmnModel);
      modelBytes = modelNode.toString().getBytes("utf-8");
    } catch(Exception e) {
      throw new ActivitiException("Error converting BPMN source to JSON: " + e.getMessage(), e);
    }
    ActivitiUtil.getRepositoryService().addModelEditorSource(model.getId(), modelBytes);
  }
  
}
