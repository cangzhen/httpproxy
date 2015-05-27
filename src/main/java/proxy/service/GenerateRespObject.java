package proxy.service;

import java.util.concurrent.atomic.AtomicInteger;

import proxy.domain.TaskRequest;
import proxy.domain.TaskResponse;

public class GenerateRespObject {
	static AtomicInteger counter  = new AtomicInteger();
	
	public static TaskResponse getResponse()
	{
		int id = counter.getAndAdd(1);
		TaskResponse ret = new TaskResponse();
		ret.setId(id);
		ret.setType("Response");
		ret.setName("GenerateRespObject");
		ret.setData("this is an auto generate object");

		return ret;
	}
	
	public static TaskResponse getResponse(TaskRequest taskRequest)
	{
		TaskResponse ret = new TaskResponse();
		ret.setId(taskRequest.getId());
		ret.setType("Response");
		ret.setName("GenerateRespObject");
		ret.setData("this is an auto generate object,match request id "+taskRequest.getId());

		return ret;
	}
	public static TaskRequest getRequest()
	{
		int id = counter.getAndAdd(1);
		TaskRequest ret = new TaskRequest();
		ret.setId(id);
		ret.setType("Request");
		ret.setName("GenerateRespObject");
		ret.setData("this is an auto generate TaskRequest");
		return ret;
	}

}
