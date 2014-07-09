package com.cnaude.chairs.commands;

import java.util.HashSet;

public class ChairsIgnoreList {

	private static HashSet<String> ignoreList = new HashSet<String>();

	public ChairsIgnoreList() {
	}

	public void addPlayer(String s) {
		if (ignoreList.contains(s)) {
			return;
		}
		ignoreList.add(s);
	}

	public void removePlayer(String s) {
		ignoreList.remove(s);
	}

	public boolean isIgnored(String s) {
		if (ignoreList.contains(s)) {
			return true;
		}
		else {
			return false;
		}
	}
}