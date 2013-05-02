package com.sanxing.sesame.util;

import java.io.PrintWriter;

public class IndentPrinter {
	private int indentLevel;
	private String indent;
	private PrintWriter out;

	public IndentPrinter() {
		this(new PrintWriter(System.out), "  ");
	}

	public IndentPrinter(PrintWriter out) {
		this(out, "  ");
	}

	public IndentPrinter(PrintWriter out, String indent) {
		this.out = out;
		this.indent = indent;
	}

	public void println(Object value) {
		this.out.print(value.toString());
		this.out.println();
	}

	public void println(String text) {
		this.out.print(text);
		this.out.println();
	}

	public void print(String text) {
		this.out.print(text);
	}

	public void printIndent() {
		for (int i = 0; i < this.indentLevel; ++i)
			this.out.print(this.indent);
	}

	public void println() {
		this.out.println();
	}

	public void incrementIndent() {
		this.indentLevel += 1;
	}

	public void decrementIndent() {
		this.indentLevel -= 1;
	}

	public int getIndentLevel() {
		return this.indentLevel;
	}

	public void setIndentLevel(int indentLevel) {
		this.indentLevel = indentLevel;
	}

	public void flush() {
		this.out.flush();
	}
}