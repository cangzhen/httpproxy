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
package proxy.mock.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	
    private static final Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

	final  FullHttpRequest request;

	public HttpClientHandler(FullHttpRequest request) {
		this.request = request;
	}
	
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
        	//TODO 
        }
        if (msg instanceof LastHttpContent) {
        	TokenMgr.getInstance().returnToken();
        	ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	TokenMgr.getInstance().returnToken();
    	logger.warn("HttpClientHandler  exceptionCaught ",cause);
        ctx.close();
    }
}
