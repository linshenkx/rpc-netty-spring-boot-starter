package com.github.linshenkx.rpcnettycommon.exception.remoting;

public class RemotingTimeoutException extends RemotingException {


	public RemotingTimeoutException(String message) {
        super(message);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis) {
        this(addr, timeoutMillis, null);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}
