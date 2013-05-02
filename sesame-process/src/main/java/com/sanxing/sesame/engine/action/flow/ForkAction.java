package com.sanxing.sesame.engine.action.flow;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.context.DataContext;
import java.io.PrintStream;
import org.jdom2.Element;

public class ForkAction extends AbstractAction {
	Element config;

	public void doinit(Element config) {
		this.config = config;
	}

	public void dowork(DataContext ctx) {
	}

	public static void main(String[] args) {
		try {
			Runnable run = new Runnable() {
				public void run() {
					System.out.println("hello");
				}
			};
			Thread worker = new Thread(run);
			worker.start();
			Thread.currentThread();
			Thread.sleep(1000L);
			worker.join();
			System.out.println("ok");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}