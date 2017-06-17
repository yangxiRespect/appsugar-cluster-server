package org.appsugar.cluster.service.domain;

import java.io.Serializable;

/**
 * 命令消息
 * @author NewYoung
 * 2017年5月10日上午10:09:11
 */
public class CommandMessage implements Serializable {
	private static final long serialVersionUID = 9042847546659513785L;
	/**关闭事件**/
	public static final String CLOSE_COMMAND = "CLOSE";
	public static final CommandMessage CLOSE = new CommandMessage(CLOSE_COMMAND);
	/**查询动态服务**/
	public static final String QUERY_DYNAMIC_SERVICE_COMMAND = "QUERY_DYNAMIC_SERVICE";
	private String cmd;
	private String param;

	public CommandMessage() {
		super();
	}

	public CommandMessage(String cmd) {
		super();
		this.cmd = cmd;
	}

	public CommandMessage(String cmd, String param) {
		super();
		this.cmd = cmd;
		this.param = param;
	}

	public String getCmd() {
		return cmd;
	}

	public String getParam() {
		return param;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommandMessage [cmd=").append(cmd).append(", param=").append(param).append("]");
		return builder.toString();
	}

}
