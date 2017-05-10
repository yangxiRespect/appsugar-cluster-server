package org.appsugar.cluster.service.domain;

/**
 * 命令消息
 * @author NewYoung
 * 2017年5月10日上午10:09:11
 */
public class CommandMessage {
	/**关闭事件**/
	public static final String CLOSE_COMMAND = "CLOSE";
	public String cmd;

	public CommandMessage() {
		super();
	}

	public CommandMessage(String cmd) {
		super();
		this.cmd = cmd;
	}

	public String getCmd() {
		return cmd;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommandMessage [cmd=").append(cmd).append("]");
		return builder.toString();
	}

}
