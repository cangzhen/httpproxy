/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package proxy.mock.workstation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.domain.TaskResponse;
import proxy.mock.client.HttpClientBatch;
import proxy.service.GenerateRespObject;
import proxy.util.HttpUitls;

import com.google.gson.Gson;

class HttpServerDelayHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientBatch.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
      	int sleepTime = new Random().nextInt(5*1000);
    	logger.info("receive an request,response after "+sleepTime + " ms!");
    	TaskResponse taskResponse = GenerateRespObject.getResponse();
    	taskResponse.setData(taskResponse.getData() + ". I send the response after sleep  "+sleepTime+ " ms");
    	String content = new Gson().toJson(taskResponse);
        FullHttpResponse response = HttpUitls.createHttpResponse(HttpResponseStatus.OK,content); 
        InprogressMessageQueue.getInstance().addRequest(new DelayObject(ctx,response,System.currentTimeMillis()+sleepTime));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
        	
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
   
    
}
