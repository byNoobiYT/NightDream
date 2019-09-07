/*
 * Copyright (c) JDiscordBots 2019
 * File: Eval.java
 * Project: NightDream
 * Licenced under Boost Software License 1.0
 */

package io.github.jdiscordbots.nightdream.commands;

import io.github.jdiscordbots.nightdream.logging.LogType;
import io.github.jdiscordbots.nightdream.logging.NDLogger;
import io.github.jdiscordbots.nightdream.util.JDAUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@BotCommand("eval")
public class Eval implements Command {

	private static final ScriptEngine se;
	private static final String LATEST_EXCEPTION_KEY_NAME="err";
	
	static {
		se = new ScriptEngineManager().getEngineByName("Nashorn");
		se.put("System", System.class);
        try {
			se.eval("System=System.static");
		} catch (ScriptException e) {
			NDLogger.logWithoutModule(LogType.WARN, "An Exception occurred while setting up System in eval", e);
		}
	}
	@Override
	public boolean allowExecute(String[] args, GuildMessageReceivedEvent event) {
		return JDAUtils.checkOwner(event,args!=null);	
	}
	@Override
	public void action(String[] args, GuildMessageReceivedEvent event) {
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());
        se.put("message", event.getMessage());

        StringBuilder scriptBuilder = new StringBuilder();
        for (String string : args) {
			scriptBuilder.append(string).append(" ");
		}
		try {
			onSuccess(se.eval(scriptBuilder.toString()),event);
		} catch (ScriptException|RuntimeException e) {
			se.put(LATEST_EXCEPTION_KEY_NAME, e);
			onError(e,event);
		}
	}
	
	protected void onSuccess(Object result,GuildMessageReceivedEvent event) {
		if (result != null) {
        	event.getChannel().sendMessage("```js\n"+result.toString()+"\n```").queue();
		} else {
			event.getChannel().sendMessage("`undefined` or `null`").queue();
		}
	}
	protected void onError(Exception e,GuildMessageReceivedEvent event) {
		try(StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw)){
			e.printStackTrace(pw);
			String exStr = sw.getBuffer().toString();
			int len = exStr.length();
			if(len > 1000) {
				len = 1000;
			}
			event.getChannel().sendMessage("`ERROR`\n```java\n" + exStr.substring(0, len) + "\n```").queue();
		} catch (IOException ignored) {
			NDLogger.logWithoutModule(LogType.ERROR, "Error within incorrect user input/eval execution error handling", e);
		}
	}
	@Override
	public String help() {
		return "Evaluates JS Code within Java (why JS????????)";
	}

	@Override
    public String permNeeded() {
    	return "Bot-Admin";
    }
	
	@Override
	public CommandType getType() {
		return CommandType.META;
	}
}
