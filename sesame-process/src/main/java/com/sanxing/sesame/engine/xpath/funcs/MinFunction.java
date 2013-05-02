package com.sanxing.sesame.engine.xpath.funcs;

import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.NumberFunction;

public class MinFunction implements Function {
	public Object call(Context context, List args) throws FunctionCallException {
		if (args.isEmpty())
			return Double.valueOf((0.0D / 0.0D));

		Navigator navigator = context.getNavigator();
		double min = 1.7976931348623157E+308D;
		Iterator iterator = args.iterator();
		while (iterator.hasNext()) {
			double next = NumberFunction.evaluate(iterator.next(), navigator)
					.doubleValue();
			min = Math.min(min, next);
		}
		return new Double(min);
	}
}