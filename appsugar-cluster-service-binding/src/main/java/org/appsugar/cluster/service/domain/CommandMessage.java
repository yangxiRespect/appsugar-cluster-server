package org.appsugar.cluster.service.domain;

/**
 * 命令消息
 * @author NewYoung
 * 2017年5月10日上午10:09:11
 */
public class CommandMessage {
	/**关闭事件**/
	public static final String CLOSE_COMMAND = "CLOSE";
	public static final String QUERY_DYNAMIC_SERVICE_COMMAND = "QUERY_DYNAMIC_SERVICE";
	public static final CommandMessage CLOSE = new CommandMessage(CLOSE_COMMAND);
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
