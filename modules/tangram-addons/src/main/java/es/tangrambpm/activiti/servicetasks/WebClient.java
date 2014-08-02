package es.tangrambpm.activiti.servicetasks;

import org.activiti.engine.delegate.Expression;

import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.*;

public class WebClient implements JavaDelegate
{
    protected Expression http_url;
    protected Expression http_method;
    protected Expression http_body;
    protected Expression http_user;
    protected Expression http_password;
    protected Expression http_content_type;
    protected Expression http_charset;
    protected Expression http_result_status;
    protected Expression http_result_body;

    protected String val(Expression expr, DelegateExecution exe){
        try {
            return (String) expr.getValue(exe);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void execute(DelegateExecution exe) throws Exception {
    	HttpMethodBase method=null;
        String url = expr2str(http_url, exe);
        String m = expr2str(http_method, exe);
        String body = expr2str(http_body, exe);
        String user = expr2str(http_user, exe);
        String password  = expr2str(http_password, exe);
        String content_type = expr2str(http_content_type, exe);
        String charset = expr2str(http_charset, exe);
        String result_status = expr2str(http_result_status, "http_status", exe);
        String result_body = expr2str(http_result_body, "http_result", exe);
        String result = "";
        String status = "";
        if (m.equals("")) {
            if (body.equals(""))
                m = "GET";
            else
                m = "POST";
        }
        try {
            HttpClient client = new HttpClient();
            if (m.equals("PUT"))
            	method = new PutMethod(url);
            else if (m.equals("POST"))
            	method = new PostMethod(url);
            else if (m.equals("DELETE"))
            	method = new DeleteMethod(url);
            else
            	method = new GetMethod(url);
            if (user.equals("")) {
            	Credentials creds = new UsernamePasswordCredentials(user, password);
            	client.getState().setCredentials(new AuthScope(
            			AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
            }
            if (! body.equals("")) {
            	StringRequestEntity entity = new StringRequestEntity(body,content_type, charset);
                ((EntityEnclosingMethod) method).setRequestEntity(entity);
            }
            client.executeMethod(method);
            result = method.getResponseBodyAsString();
            status = "" + method.getStatusCode();
        } catch (Exception e) {
            status = "ERROR";
            result = e.getMessage();
        } finally {
            method.releaseConnection();
            exe.setVariable(result_body, result);
            exe.setVariable(result_status, status);
        }
    }

    protected String expr2str(Expression expression, DelegateExecution execution) {
        try {
            return expression.getValue(execution).toString();
        } catch (Exception e) {
            return "";
        }
    }
    protected String expr2str(Expression expression, String defaultValue, DelegateExecution execution) {
        String res = expr2str(expression, execution);
        if (res.equals(""))
            return defaultValue;
        return res;
    }
}
