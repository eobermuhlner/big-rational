package ch.obermuhlner.math.rational.example;

import static ch.obermuhlner.math.rational.BigRational.ONE;
import static ch.obermuhlner.math.rational.BigRational.ZERO;
import static ch.obermuhlner.math.rational.BigRational.log;
import static ch.obermuhlner.math.rational.BigRational.pi;
import static ch.obermuhlner.math.rational.BigRational.sin;
import static ch.obermuhlner.math.rational.BigRational.valueOf;

import ch.obermuhlner.math.rational.BigRational;

public class SimpleExamples {

	public static void main(String[] args) {
//		exampleHelloRational();
//		exampleSin();
		exampleLog();
//		examplePiPrecision();
//		examplePiHighPrecision();
//		examplePiSimple();

	}

	private static void exampleHelloRational() {
		System.out.println("Hello Rational");
		
		BigRational r1 = valueOf(1).divide(valueOf(3));
		BigRational r2 = r1.multiply(3);
		
		System.out.println("1/3   = " + r1);
		System.out.println("1/3*3 = " + r2);
		System.out.println();
	}

	private static void exampleSin() {
		System.out.println("Sinus calculations");

		BigRational x = ZERO;
		BigRational step = valueOf(1, 10);
		int precision = 20;
		
		while (x.compareTo(ONE) < 0) {
			System.out.println("sin(" + x + ") = " + sin(x, precision));
			x = x.add(step);
		}
		System.out.println();
	}

	private static void exampleLog() {
		System.out.println("Log calculations");

		BigRational x = valueOf(1, 10);
		BigRational step = valueOf(1, 10);
		BigRational end = valueOf(10);
		int precision = 20;
		
		while (x.compareTo(end) < 0) {
			System.out.println("ln(" + x + ") = " + log(x, precision));
			x = x.add(step);
		}
		System.out.println();
	}

	private static void examplePiPrecision() {
		System.out.println("Pi at various precisions");

		for (int precision = 0; precision <= 1000; precision+=100) {
			StopWatch stopWatch = new StopWatch();
			BigRational pi = pi(precision);
			stopWatch.stop();
			System.out.println("Pi(" + precision + ") = " + pi);
			System.out.println("in " + stopWatch);
		}
		System.out.println();
	}

	private static void examplePiHighPrecision() {
		System.out.println("Pi at high precision");

		StopWatch stopWatch = new StopWatch();
		int precision = 1000;
		BigRational pi = pi(precision);
		stopWatch.stop();
		System.out.println("Pi(" + precision + "):");
		printPi(pi.toString(), 100);
		System.out.println("in " + stopWatch);
		System.out.println();
	}

	private static void printPi(String string, int n) {
		System.out.println(string.substring(0, 2));
		for (int i = 2; i < string.length(); i+=n) {
			System.out.println(string.substring(i, Math.min(i + n, string.length())));
		}
	}

	private static void examplePiSimple() {
		BigRational minusOne = BigRational.ONE.negate();
		BigRational step = BigRational.valueOf(2);
		BigRational result = BigRational.ZERO;
		BigRational divisor = BigRational.ONE;
		for (int i = 0; i < 200; i++) {
			result = result.add(BigRational.ONE.divide(divisor));
			divisor = divisor.add(step);

			BigRational intermediateResult = result;
			result = result.add(minusOne.divide(divisor));
			divisor = divisor.add(step);

			intermediateResult = intermediateResult.add(result);
			System.out.println(i + " : " + intermediateResult.multiply(BigRational.TWO));
		}
	}
	
	private static class StopWatch {
		private long startMillis;
		private long endMillis;

		public StopWatch() {
			start();
		}

		public void start() {
			startMillis = System.currentTimeMillis();
		}
		
		public void stop() {
			endMillis = System.currentTimeMillis();
		}
		
		public long getElapsedMillis() {
			return endMillis - startMillis;
		}
		
		@Override
		public String toString() {
			if (endMillis < startMillis) {
				stop();
			}

			return getElapsedMillis() + " ms";
		}
	}
}
