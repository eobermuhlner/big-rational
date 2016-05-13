package ch.obermuhlner.math.rational.example;

import java.math.BigDecimal;
import java.math.MathContext;

import ch.obermuhlner.math.rational.BigRational;
import static ch.obermuhlner.math.rational.BigRational.*;

public class MeasureCpu {

	private static final int N = 100;

	private static MathContext MC = new MathContext(50);
	
	public static void main(String[] args) {
		warmup();

		for (int i = 0; i < 2; i++) {
			measureAdd();
			measureBigDecimalAdd();
			measureSubtract();
			measureBigDecimalSubtract();
			measureMultiply();
			measureBigDecimalMultiply();
			measureDivide();
			measureBigDecimalDivide();
			
			System.out.println();
		}

		//measurePi();
	}

	private static void warmup() {
		for (int i = 0; i < 10000; i++) {
			BigRational.ZERO.add(createBigRational(i));
			BigDecimal.ZERO.add(createBigDecimal(i));
		}
	}

	private static void measureAdd() {
		StopWatch watch = new StopWatch();
		
		BigRational r = ZERO;
		for (int i = 0; i < N; i++) {
			r = r.add(createBigRational(i));
		}
		
		System.out.printf("BigRational: %d times %-15s : %-14s | Result: %s\n", N, "add()", watch, r);
	}

	private static void measureBigDecimalAdd() {
		StopWatch watch = new StopWatch();
		
		BigDecimal r = BigDecimal.ZERO;
		for (int i = 0; i < N; i++) {
			r = r.add(createBigDecimal(i));
		}
		
		System.out.printf("BigDecimal : %d times %-15s : %-14s | Result: %s\n", N, "add()", watch, r);
	}

	private static void measureSubtract() {
		StopWatch watch = new StopWatch();
		
		BigRational r = ZERO;
		for (int i = 0; i < N; i++) {
			r = r.subtract(createBigRational(i));
		}
		
		System.out.printf("BigRational: %d times %-15s : %-14s | Result: %s\n", N, "subtract()", watch, r);
	}
	
	private static void measureBigDecimalSubtract() {
		StopWatch watch = new StopWatch();
		
		BigDecimal r = BigDecimal.ZERO;
		for (int i = 0; i < N; i++) {
			r = r.subtract(createBigDecimal(i));
		}
		
		System.out.printf("BigDecimal : %d times %-15s : %-14s | Result: %s\n", N, "subtract()", watch, r);
	}
	
	private static void measureMultiply() {
		StopWatch watch = new StopWatch();
		
		BigRational r = ONE;
		for (int i = 0; i < N; i++) {
			r = r.multiply(createBigRational(i));
		}
		
		System.out.printf("BigRational: %d times %-15s : %-14s | Result: %s\n", N, "multiply()", watch, r);
	}
	
	private static void measureBigDecimalMultiply() {
		StopWatch watch = new StopWatch();
		
		BigDecimal r = BigDecimal.ONE;
		for (int i = 0; i < N; i++) {
			r = r.multiply(createBigDecimal(i));
		}
		
		System.out.printf("BigDecimal : %d times %-15s : %-14s | Result: %s\n", N, "multiply()", watch, r);
	}
	
	private static void measureDivide() {
		StopWatch watch = new StopWatch();
		
		BigRational r = ONE;
		for (int i = 0; i < N; i++) {
			r = r.divide(createBigRational(i));
		}
		
		System.out.printf("BigRational: %d times %-15s : %-14s | Result: %s\n", N, "divide()", watch, r);
	}
	
	private static void measureBigDecimalDivide() {
		StopWatch watch = new StopWatch();
		
		BigDecimal r = BigDecimal.ONE;
		for (int i = 0; i < N; i++) {
			r = r.divide(createBigDecimal(i), MC);
		}
		
		System.out.printf("BigDecimal : %d times %-15s : %-14s | Result: %s\n", N, "divide()", watch, r);
	}
	
	private static void measurePi() {
		StopWatch watch = new StopWatch();
		
		final int precision = 1000;
		BigRational r = pi(precision);
		
		System.out.printf("BigRational: %d precision %-15s : %-14s\n", precision, "pi()", watch);
	}
	
	private static BigRational createBigRational(int index) {
		return BigRational.valueOf(index + 1);
	}

	private static BigDecimal createBigDecimal(int index) {
		return BigDecimal.valueOf(index + 1);
	}

	private static class StopWatch {
		private long startTime = System.nanoTime();

		double getElapsedMilliseconds() {
			long endTime = System.nanoTime();
			
			return (endTime - startTime) / 1000000.0;
		}

		@Override
		public String toString() {
			return getElapsedMilliseconds() + " ms";
		}
	}
}
