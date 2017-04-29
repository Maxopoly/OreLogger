package com.github.maxopoly.logging;

import java.util.List;

/**
 * Creates logs, which can be grabbed by an IO class
 *
 */
public interface LogProvider {

	public List<String> pullPendingMessagesAndFlush();

}
