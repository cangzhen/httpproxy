package proxy.mock.workstation;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 正在处理中，还未完成处理的请求消息队列 包括节点发出的请求，以及收到的请求。
 * 
 * @author lcz
 * 
 */
class InprogressMessageQueue  {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(InprogressMessageQueue.class);
	/**
	 * 当前允许的消息处理最大并发数
	 */
	private volatile int maxInprogressCount = 10000;
	/**
	 * DCCProgress 超时时间（秒）
	 */
	private volatile int dccProgressTimeoutSeconds = 30;

	/**
	 * 守护线程扫描的间隔时间
	 */
	private volatile int watchDogScanSecondes = 1;
	/**
	 * 队列容量告警阀值
	 */
	private volatile int queueWarnThreshold = 66;


	/**
	 * 保存当前正在处理中的消息状态
	 */
	public static ConcurrentHashMap<DelayObject, DelayObject> msgProgressMap = new ConcurrentHashMap<DelayObject, DelayObject>();

	WatchDogThread watchDogThread;

	/**
	 * 所关心的运行期变化的配置数据的Key
	 */
	private volatile long prevQuequeWarnedTime;

	static InprogressMessageQueue instance;
	public static InprogressMessageQueue getInstance() {
		if (instance == null) {
			
			instance = new  InprogressMessageQueue();
		}
		return instance;
	}

	/**
	 * 初始化过程
	 */
	public void init() {
		watchDogThread = new WatchDogThread();
		watchDogThread.start();
	}


	/**
	 * 队列使用率 0-100之间
	 * 
	 * @return
	 */
	public double getQueueUsage() {
		return 100 * (msgProgressMap.size() + 0.0) / maxInprogressCount;
	}

	/**
	 * 取队列最大长度
	 * 
	 * @return
	 */
	public int getMaxInprogressCount() {
		return maxInprogressCount;
	}

	/**
	 * 第一个元素是队列当前长度，第二个元素是所有请求的数据字节长度总和
	 * 
	 * @return
	 */
	public int getQueueSize() {
		return msgProgressMap.size();
	}

	/**
	 * 添加一个正在处理的新请求
	 * 
	 * @param msgProgress
	 */
	public void addRequest(DelayObject callParam) {
		msgProgressMap.put(callParam, callParam);
	}

	/**
	 * 将原有的某个msg process移除
	 * 
	 * @param oldMsgID
	 */
	public DelayObject removeProgress(DelayObject oldMsgID) {
		return msgProgressMap.remove(oldMsgID);
	}
	/**
	 * 得到当前队列中各种请求的Pending的消息数目
	 * 
	 * @return 队列长度
	 */
	public int[] getPendingCounts() {
		int[] totalCounts = new int[3];
		return totalCounts;
	}


	public void shutdown() {
		watchDogThread.interrupt();
	}

	class WatchDogThread extends Thread {
		public WatchDogThread() {
			setName("InprogressMessageQueue-WatchDogThread");
			this.setDaemon(true);
		}

		public void run() {
			while (!Thread.interrupted()) {
				checkQueue();
				try {
					Thread.sleep(watchDogScanSecondes * 1000);
				} catch (Exception e) {

				}
			}
			logger.info("shutdowned");
		}

		/**
		 * 
		 */
		private void checkQueue() {
			try {
				int queueLen = 0;
				Iterator<Map.Entry<DelayObject, DelayObject>> itor = msgProgressMap.entrySet().iterator();
				long curTime = System.currentTimeMillis();
				while (itor.hasNext()) {
					queueLen++;
					Map.Entry<DelayObject, DelayObject> entry = itor.next();
					DelayObject msgPrgrss = entry.getValue();
					if (msgPrgrss.sendTime < curTime) {// 已经超时
						final ChannelHandlerContext ctx = msgPrgrss.ctx;
						ctx.writeAndFlush(msgPrgrss.response).addListener(new ChannelFutureListener() {

				            @Override
				            public void operationComplete(ChannelFuture future) throws Exception {
				                if (!future.isSuccess()) {
									logger.warn("return reponse to frontend failed."+future.cause().getMessage());
				                }
				                ctx.close();
				            }
				        });
						// 从队列中删除,并调用Process处理应答
						itor.remove();
					}
				}
				// 检查是否队列容量超阀值
				if (queueLen * queueWarnThreshold / 100 > maxInprogressCount && prevQuequeWarnedTime + 5 * 60 * 1000 < curTime) {
					logger.warn("|current queue reached threshold " + queueWarnThreshold + ",cur length:" + queueLen + ",max capacity:" + maxInprogressCount);
					prevQuequeWarnedTime = curTime;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("checkQueue:" + e.toString());
			}
		}
	}


}
