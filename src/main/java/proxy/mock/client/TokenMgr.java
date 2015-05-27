package proxy.mock.client;

import java.util.concurrent.locks.ReentrantLock;

public class TokenMgr {
    ReentrantLock lock = new ReentrantLock();
    volatile int tokenNum = 500;
    
	public int getTokenNum() {
		return tokenNum;
	}
	public void setTokenNum(int tokenNum) {
		this.tokenNum = tokenNum;
	}
	static TokenMgr instance;

	public static TokenMgr getInstance() {
		if (instance == null) {
			
			instance = new  TokenMgr();
		}
		return instance;
	}
    boolean getToken()
    {
    	boolean isHasToken= true;
    	lock.lock();
    	if(tokenNum > 0)
    	{
    		--tokenNum;
    	}
    	else
    	{
    		isHasToken = false;
    	}
    	lock.unlock();
    	return isHasToken;
    }
    void returnToken()
    {

    	lock.lock();
    		++tokenNum;
    	lock.unlock();
    }
}
