package ch.obermuhlner.math.rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * A rational number represented as a quotient of two values.
 * 
 * <p>Basic calculations with rational numbers (+ - * /) have no loss of precision.
 * This allows to use {@link BigRational} as a replacement for {@link BigDecimal} if absolute accuracy is desired.</p>
 * 
 * <p><a href="http://en.wikipedia.org/wiki/Rational_number">Wikipedia: Rational number</a></p>
 * 
 * <p>The values are internally stored as {@link BigDecimal} (for performance optimizations) but represented
 * as {@link BigInteger} (for mathematical correctness)
 * when accessed with {@link #getNumerator()} and {@link #getDenominator()}.</p>
 * 
 * <p>The following basic calculations have no loss of precision:
 * <ul>
 * <li>{@link #add(BigRational)}</li>
 * <li>{@link #subtract(BigRational)}</li>
 * <li>{@link #multiply(BigRational)}</li>
 * <li>{@link #divide(BigRational)}</li>
 * <li>{@link #pow(int)}</li>
 * <li>{@link #pow(BigRational, int)} with integer arguments</li>
 * </ul>
 * </p>
 * 
 * <p>The following calculations are special cases of the ones listed above and have no loss of precision:
 * <ul>
 * <li>{@link #negate()}</li>
 * <li>{@link #reciprocal()}</li>
 * <li>{@link #increment()}</li>
 * <li>{@link #decrement()}</li>
 * <li>{@link #factorial(int)}</li>
 * </ul>
 * </p>
 * 
 * <p>The following calculations must specify the desired precision:
 * <ul>
 * <li>{@link #pow(BigRational, int)} with non-integer arguments</li>
 * <li>{@link #exp(BigRational, int)}</li>
 * <li>{@link #log(BigRational, int)}</li>
 * <li>{@link #sin(BigRational, int)}</li>
 * <li>{@link #cos(BigRational, int)}</li>
 * <li>{@link #pi(int)} (constant with the specified precision)</li>
 * </ul>
 * </p>
 * 
 * <p>Any {@link BigRational} value can be converted into an arbitrary {@link #withPrecision(int) precision} (number of significant digits)
 * or {@link #withScale(int) scale} (number of digits after the decimal point).</p>
 */
public class BigRational implements Comparable<BigRational> {

	/**
	 * The value 0 as {@link BigRational}.
	 */
	public static final BigRational ZERO = new BigRational(0);
	/**
	 * The value 1 as {@link BigRational}.
	 */
	public static final BigRational ONE = new BigRational(1);
	/**
	 * The value 2 as {@link BigRational}.
	 */
	public static final BigRational TWO = new BigRational(2);
	/**
	 * The value 10 as {@link BigRational}.
	 */
	public static final BigRational TEN = new BigRational(10);

	/**
	 * The default {@link Context} with a scale of 16.
	 */
	public static final Context DEFAULT = new Context(16);

	private static final BigDecimal BIGDECIMAL_MAX_INT = BigDecimal.valueOf(Integer.MAX_VALUE);

	private static BigRational[] factorialCache = new BigRational[100];
	static {
		BigRational result = ONE;
		factorialCache[0] = result;
		for (int i = 1; i < factorialCache.length; i++) {
			result = result.multiply(valueOf(i));
			factorialCache[i] = result;
		}
	}

	private static BigRational[] bernoulliCache = new BigRational[11];
	static {
		bernoulliCache[0 / 2] = valueOf(1);
		bernoulliCache[2 / 2] = valueOf(1, 6);
		bernoulliCache[4 / 2] = valueOf(-1, 30);
		bernoulliCache[6 / 2] = valueOf(1, 42);
		bernoulliCache[8 / 2] = valueOf(-1, 30);
		bernoulliCache[10 / 2] = valueOf(5, 66);
		bernoulliCache[12 / 2] = valueOf(-691, 2730);
		bernoulliCache[14 / 2] = valueOf(7, 6);
		bernoulliCache[16 / 2] = valueOf(-3617, 510);
		bernoulliCache[18 / 2] = valueOf(43867, 798);
		bernoulliCache[20 / 2] = valueOf(-17611, 330);
	}

	private final BigDecimal numerator;

	private final BigDecimal denominator;

	private BigRational(int value) {
		this(BigDecimal.valueOf(value), BigDecimal.ONE);
	}

	private BigRational(BigDecimal num, BigDecimal denom) {
		BigDecimal n = num;
		BigDecimal d = denom;

		if (d.signum() == 0) {
			throw new ArithmeticException("Divide by zero");
		}

		if (d.signum() < 0) {
			n = n.negate();
			d = d.negate();
		}

		numerator = n;
		denominator = d;
	}

	/**
	 * Returns the numerator of this rational number.
	 * 
	 * @return the numerator
	 */
	public BigInteger getNumerator() {
		return numerator.toBigInteger();
	}

	/**
	 * Returns the denominator of this rational number.
	 * 
	 * <p>Guaranteed to not be 0.</p>
	 * <p>Guaranteed to be positive.</p>
	 * 
	 * @return the denominator
	 */
	public BigInteger getDenominator() {
		return denominator.toBigInteger();
	}

	/**
	 * Reduces this rational number to the smallest numerator/denominator with the same value.
	 * 
	 * @return the reduced rational number
	 */
	public BigRational reduce() {
		BigInteger n = numerator.toBigInteger();
		BigInteger d = denominator.toBigInteger();

		BigInteger gcd = n.gcd(d);
		n = n.divide(gcd);
		d = d.divide(gcd);

		return valueOf(n, d);
	}

	/**
	 * Returns the integer part of this rational number.
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(3.5).integerPart()</code> returns <code>BigRational.valueOf(3)</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the integer part of this rational number
	 */
	public BigRational integerPart() {
		return valueOf(numerator.subtract(numerator.remainder(denominator)), denominator);
	}

	/**
	 * Returns the fraction part of this rational number.
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(3.5).integerPart()</code> returns <code>BigRational.valueOf(0.5)</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the fraction part of this rational number
	 */
	public BigRational fractionPart() {
		return valueOf(numerator.remainder(denominator), denominator);
	}
	
	/**
	 * Negates this rational number (inverting the sign).
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(3.5).negate()</code> returns <code>BigRational.valueOf(-3.5)</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the negated rational number
	 */
	public BigRational negate() {
		if (isZero()) {
			return this;
		}

		return valueOf(numerator.negate(), denominator);
	}

	/**
	 * Calculates the reciprocal of this rational number (1/x).
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(0.5).reciprocal()</code> returns <code>BigRational.valueOf(2)</code></li>
	 * <li><code>BigRational.valueOf(-2).reciprocal()</code> returns <code>BigRational.valueOf(-0.5)</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the reciprocal rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational reciprocal() {
		return valueOf(denominator, numerator);
	}

	/**
	 * Returns the absolute value of this rational number.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(-2).abs()</code> returns <code>BigRational.valueOf(2)</code></li>
	 * <li><code>BigRational.valueOf(2).abs()</code> returns <code>BigRational.valueOf(2)</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the absolute rational number (positive, or 0 if this rational is 0)
	 */
	public BigRational abs() {
		return isPositive() ? this : negate();
	}

	/**
	 * Returns the signum function of this rational number.
	 *
	 * @return -1, 0 or 1 as the value of this rational number is negative, zero or positive.
	 */
	public int signum() {
		return numerator.signum();
	}

	/**
	 * Calculates the increment of this rational number (+ 1).
	 * 
	 * <p>This is functionally identical to
	 * <code>this.add(BigRational.ONE)</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @return the incremented rational number
	 */
	public BigRational increment() {
		return valueOf(numerator.add(denominator), denominator);
	}

	/**
	 * Calculates the decrement of this rational number (- 1).
	 * 
	 * <p>This is functionally identical to
	 * <code>this.subtract(BigRational.ONE)</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @return the decremented rational number
	 */
	public BigRational decrement() {
		return valueOf(numerator.subtract(denominator), denominator);
	}

	/**
	 * Calculates the addition (+) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to add
	 * @return the resulting rational number
	 */
	public BigRational add(BigRational value) {
		if (denominator.equals(value.denominator)) {
			return valueOf(numerator.add(value.numerator), denominator);
		}

		BigDecimal n = numerator.multiply(value.denominator).add(value.numerator.multiply(denominator));
		BigDecimal d = denominator.multiply(value.denominator);
		return valueOf(n, d);
	}

	private BigRational add(BigDecimal value) {
		return valueOf(numerator.add(value.multiply(denominator)), denominator);
	}
	
	/**
	 * Calculates the addition (+) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.add(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to add
	 * @return the resulting rational number
	 */
	public BigRational add(BigInteger value) {
		if (value.equals(BigInteger.ZERO)) {
			return this;
		}
		return add(new BigDecimal(value));
	}

	/**
	 * Calculates the addition (+) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.add(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to add
	 * @return the resulting rational number
	 */
	public BigRational add(int value) {
		if (value == 0) {
			return this;
		}
		return add(BigInteger.valueOf(value));
	}

	/**
	 * Calculates the subtraction (-) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to subtract
	 * @return the resulting rational number
	 */
	public BigRational subtract(BigRational value) {
		if (denominator.equals(value.denominator)) {
			return valueOf(numerator.subtract(value.numerator), denominator);
		}

		BigDecimal n = numerator.multiply(value.denominator).subtract(value.numerator.multiply(denominator));
		BigDecimal d = denominator.multiply(value.denominator);
		return valueOf(n, d);
	}

	private BigRational subtract(BigDecimal value) {
		return valueOf(numerator.subtract(value.multiply(denominator)), denominator);
	}
	
	/**
	 * Calculates the subtraction (-) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.subtract(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to subtract
	 * @return the resulting rational number
	 */
	public BigRational subtract(BigInteger value) {
		if (value.equals(BigInteger.ZERO)) {
			return this;
		}
		return subtract(new BigDecimal(value));
	}

	/**
	 * Calculates the subtraction (-) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.subtract(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to subtract
	 * @return the resulting rational number
	 */
	public BigRational subtract(int value) {
		if (value == 0) {
			return this;
		}
		return subtract(BigInteger.valueOf(value));
	}

	/**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(BigRational value) {
		if (isZero() || value.isZero()) {
			return ZERO;
		}
		if (equals(ONE)) {
			return value;
		}
		if (value.equals(ONE)) {
			return this;
		}

		BigDecimal n = numerator.multiply(value.numerator);
		BigDecimal d = denominator.multiply(value.denominator);
		return valueOf(n, d);
	}

	// private, because we want to hide that we use BigDecimal internally
	private BigRational multiply(BigDecimal value) {
		BigDecimal n = numerator.multiply(value);
		BigDecimal d = denominator;
		return valueOf(n, d);
	}
	
	/**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.multiply(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(BigInteger value) {
		if (isZero() || value.signum() == 0) {
			return ZERO;
		}
		if (equals(ONE)) {
			return valueOf(value);
		}
		if (value.equals(BigInteger.ONE)) {
			return this;
		}

		return multiply(new BigDecimal(value));
	}

	/**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.multiply(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(int value) {
		return multiply(BigInteger.valueOf(value));
	}

	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(BigRational value) {
		if (value.equals(ONE)) {
			return this;
		}

		BigDecimal n = numerator.multiply(value.denominator);
		BigDecimal d = denominator.multiply(value.numerator);
		return valueOf(n, d);
	}

	private BigRational divide(BigDecimal value) {
		BigDecimal n = numerator;
		BigDecimal d = denominator.multiply(value);
		return valueOf(n, d);
	}
	
	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.divide(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(BigInteger value) {
		if (value.equals(BigInteger.ONE)) {
			return this;
		}

		return divide(new BigDecimal(value));
	}

	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.divide(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(int value) {
		return divide(BigInteger.valueOf(value));
	}

	/**
	 * Returns whether this rational number is zero.
	 * 
	 * @return <code>true</code> if this rational number is zero (0), <code>false</code> if it is not zero
	 */
	public boolean isZero() {
		return numerator.signum() == 0;
	}

	private boolean isOne() {
		return numerator.equals(denominator);
	}

	private boolean isPositive() {
		return numerator.signum() > 0;
	}

	/**
	 * Returns whether this rational number is an integer number without fraction part.
	 * 
	 * @return <code>true</code> if this rational number is an integer number, <code>false</code> if it has a fraction part
	 */
	public boolean isInteger() {
		return isIntegerInternal() || reduce().isIntegerInternal();
	}

	/**
	 * Returns whether this rational number is an integer number without fraction part.
	 * 
	 * <p>Will return <code>false</code> if this number is not reduced to the integer representation yet (e.g. 4/4 or 4/2)</p>
	 * 
	 * @return <code>true</code> if this rational number is an integer number, <code>false</code> if it has a fraction part
	 * @see #isInteger()
	 */
	private boolean isIntegerInternal() {
		return denominator.compareTo(BigDecimal.ONE) == 0;
	}

	/**
	 * Calculates this rational number to the power (x<sup>y</sup>) of the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 *
	 * @param exponent exponent to which this rational number is to be raised
	 * @return the resulting rational number
	 * @see #pow(BigRational, int)
	 */
	public BigRational pow(int exponent) {
		if (exponent == 0) {
			return ONE;
		}
		if (exponent == 1) {
			return this;
		}

		final BigInteger n;
		final BigInteger d;
		if (exponent > 0) {
			n = numerator.toBigInteger().pow(exponent);
			d = denominator.toBigInteger().pow(exponent);
		}
		else {
			n = denominator.toBigInteger().pow(-exponent);
			d = numerator.toBigInteger().pow(-exponent);
		}
		return valueOf(n, d);
	}

	/**
	 * Calculates this rational number to the power (x<sup>y</sup>) of the specified argument.
	 * 
	 * <p>If the exponent is an integer value then the result has no loss of precision.</p>
	 * <p>If the exponent is not an integer value then the result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 *
	 * @param exponent exponent to which this rational number is to be raised
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the resulting rational number
	 * @see #pow(int)
	 */
	public BigRational pow(BigRational exponent, int scale) {
		// x^y = exp(y*log(x))
		// TODO calculate with taylor series?

		BigRational reducedExponent = exponent.reduce();
		if (reducedExponent.isIntegerInternal() && reducedExponent.numerator.compareTo(BIGDECIMAL_MAX_INT) < 0) {
			return pow(reducedExponent.numerator.intValue());
		}
		return exp(reducedExponent.multiply(log(this, scale + 4)), scale);
	}

	/**
	 * Finds the minimum (smaller) of two rational numbers.
	 * 
	 * @param value the rational number to compare with
	 * @return the minimum rational number, either <code>this</code> or the argument <code>value</code>
	 */
	private BigRational min(BigRational value) {
		return compareTo(value) <= 0 ? this : value;
	}

	/**
	 * Finds the maximum (larger) of two rational numbers.
	 * 
	 * @param value the rational number to compare with
	 * @return the minimum rational number, either <code>this</code> or the argument <code>value</code>
	 */
	private BigRational max(BigRational value) {
		return compareTo(value) >= 0 ? this : value;
	}

	/**
	 * Returns a rational number with approximatively <code>this</code> value and the specified precision.
	 * 
	 * @param precision the precision (number of significant digits) of the calculated result, or 0 for unlimited precision
	 * @return the calculated rational number with the specified precision
	 */
	public BigRational withPrecision(int precision) {
		return valueOf(toBigDecimal(new MathContext(precision)));
	}

	/**
	 * Returns a rational number with approximatively <code>this</code> value and the specified scale.
	 * 
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated rational number with the specified scale
	 */
	public BigRational withScale(int scale) {
		return valueOf(toBigDecimal().setScale(scale, RoundingMode.HALF_UP));
	}

	private static int countDigits(BigInteger number) {
		double factor = Math.log(2) / Math.log(10);
		int digitCount = (int) (factor * number.bitLength() + 1);
		if (BigInteger.TEN.pow(digitCount - 1).compareTo(number) > 0) {
			return digitCount - 1;
		}
		return digitCount;
	}

	// TODO what is precision of a rational?
	private int precision() {
		return countDigits(numerator.toBigInteger()) + countDigits(denominator.toBigInteger());
	}

	/**
	 * Returns this rational number as a double value.
	 * 
	 * @return the double value
	 */
	public double toDouble() {
		// TODO best accuracy or maybe bigDecimalValue().doubleValue() is better?
		return numerator.doubleValue() / denominator.doubleValue();
	}

	/**
	 * Returns this rational number as a float value.
	 * 
	 * @return the float value
	 */
	public float toFloat() {
		return numerator.floatValue() / denominator.floatValue();
	}

	/**
	 * Returns this rational number as a {@link BigDecimal}.
	 * 
	 * @return the {@link BigDecimal} value
	 */
	public BigDecimal toBigDecimal() {
		int precision = Math.max(precision(), MathContext.DECIMAL128.getPrecision());
		return toBigDecimal(new MathContext(precision));
	}

	/**
	 * Returns this rational number as a {@link BigDecimal} with the precision specified by the {@link MathContext}.
	 * 
	 * @param mc the {@link MathContext} specifying the precision of the calculated result
	 * @return the {@link BigDecimal}
	 */
	public BigDecimal toBigDecimal(MathContext mc) {
		return numerator.divide(denominator, mc);
	}

	@Override
	public int compareTo(BigRational other) {
		if (this == other) {
			return 0;
		}
		return numerator.multiply(other.denominator).compareTo(denominator.multiply(other.numerator));
	}

	@Override
	public int hashCode() {
		if (isZero()) {
			return 0;
		}
		return numerator.hashCode() + denominator.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof BigRational)) {
			return false;
		}

		BigRational other = (BigRational) obj;
		if (!numerator.equals(other.numerator)) {
			return false;
		}
		return denominator.equals(other.denominator);
	}

	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		if (isIntegerInternal()) {
			return numerator.toString();
		}
		return toBigDecimal().toString();
	}

	/**
	 * Returns a plain string representation of this rational number without any exponent.
	 * 
	 * @return the plain string representation
	 * @see BigDecimal#toPlainString()
	 */
	public String toPlainString() {
		if (isZero()) {
			return "0";
		}
		if (isIntegerInternal()) {
			return numerator.toPlainString();
		}
		return toBigDecimal().toPlainString();
	}

	/**
	 * Returns the string representation of this rational number in the form "numerator/denominator".
	 * 
	 * <p>The resulting string is a valid input of the {@link #valueOf(String)} method.</p>
	 * 
	 * <p>Examples:
	 * <ul>
	 * <li><code>BigRational.valueOf(0.5).toRationalString()</code> returns <code>"1/2"</code></li>
	 * <li><code>BigRational.valueOf(2).toRationalString()</code> returns <code>"2"</code></li>
	 * <li><code>BigRational.valueOf(4, 4).toRationalString()</code> returns <code>"4/4"</code> (not reduced)</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the rational number string representation in the form "numerator/denominator", or "0" if the rational number is 0.
	 * @see #valueOf(String) 
	 * @see #valueOf(int, int) 
	 */
	public String toRationalString() {
		if (isZero()) {
			return "0";
		}
		if (isIntegerInternal()) {
			return numerator.toString();
		}
		return numerator + "/" + denominator;
	}

	/**
	 * Returns the string representation of this rational number as integer and fraction parts in the form "integerPart fractionNominator/fractionDenominator".
	 * 
	 * <p>The integer part is omitted if it is 0 (when this absolute rational number is smaller than 1).</p>
	 * <p>The fraction part is omitted it it is 0 (when this rational number is an integer).</p>
	 * <p>If this rational number is 0, then "0" is returned.</p>
	 * 
	 * <p>Example: <code>BigRational.valueOf(3.5).toIntegerRationalString()</code> returns <code>"3 1/2"</code>.</p>
	 * 
	 * @return the integer and fraction rational string representation
	 * @see #valueOf(int, int, int)
	 */
	public String toIntegerRationalString() {
		BigDecimal fractionNumerator = numerator.remainder(denominator);
		BigDecimal integerNumerator = numerator.subtract(fractionNumerator);
		BigDecimal integerPart = integerNumerator.divide(denominator);

		StringBuilder result = new StringBuilder();
		if (integerPart.signum() != 0) {
			result.append(integerPart);
		}
		if (fractionNumerator.signum() != 0) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(fractionNumerator.abs());
			result.append('/');
			result.append(denominator);
		}
		if (result.length() == 0) {
			result.append('0');
		}
		
		return result.toString();
	}
	
	/**
	 * Creates a rational number of the specified int value.
	 * 
	 * @param value the int value
	 * @return the rational number
	 */
	public static BigRational valueOf(int value) {
		if (value == 0) {
			return ZERO;
		}
		if (value == 1) {
			return ONE;
		}
		return new BigRational(value);
	}

	/**
	 * Creates a rational number of the specified numerator/denominator int values.
	 * 
	 * @param numerator the numerator int value
	 * @param denominator the denominator int value (0 not allowed)
	 * @return the rational number
	 * @throws ArithmeticException if the denominator is 0 (division by zero)
	 */
	public static BigRational valueOf(int numerator, int denominator) {
		return valueOf(BigDecimal.valueOf(numerator), BigDecimal.valueOf(denominator));
	}

	/**
	 * Creates a rational number of the specified integer and fraction parts.
	 * 
	 * <p>Useful to create numbers like 3 1/2 (= three and a half = 3.5) by calling
	 * <code>BigRational.valueOf(3, 1, 2)</code>.</p>
	 * <p>To create a negative rational only the integer part argument is allowed to be negative:
	 * to create -3 1/2 (= minus three and a half = -3.5) call <code>BigRational.valueOf(-3, 1, 2)</code>.</p> 
	 * 
	 * @param integer the integer part int value
	 * @param fractionNumerator the fraction part numerator int value (negative not allowed)
	 * @param fractionDenominator the fraction part denominator int value (0 or negative not allowed)
	 * @return the rational number
	 * @throws ArithmeticException if the fraction part denominator is 0 (division by zero),
	 * or if the fraction part numerator or denominator is negative
	 */
	public static BigRational valueOf(int integer, int fractionNumerator, int fractionDenominator) {
		if (fractionNumerator < 0 || fractionDenominator < 0) {
			throw new ArithmeticException("Negative value");
		}
		
		BigRational integerPart = valueOf(integer);
		BigRational fractionPart = valueOf(fractionNumerator, fractionDenominator);
		return integerPart.isPositive() ? integerPart.add(fractionPart) : integerPart.subtract(fractionPart);
	}

	/**
	 * Creates a rational number of the specified numerator/denominator BigInteger values.
	 * 
	 * @param numerator the numerator {@link BigInteger} value
	 * @param denominator the denominator {@link BigInteger} value (0 not allowed)
	 * @return the rational number
	 * @throws ArithmeticException if the denominator is 0 (division by zero)
	 */
	public static BigRational valueOf(BigInteger numerator, BigInteger denominator) {
		return valueOf(new BigDecimal(numerator), new BigDecimal(denominator));
	}

	/**
	 * Creates a rational number of the specified {@link BigInteger} value.
	 * 
	 * @param value the {@link BigInteger} value
	 * @return the rational number
	 */
	public static BigRational valueOf(BigInteger value) {
		if (value.compareTo(BigInteger.ZERO) == 0) {
			return ZERO;
		}
		if (value.compareTo(BigInteger.ONE) == 0) {
			return ONE;
		}
		return valueOf(value, BigInteger.ONE);
	}

	/**
	 * Creates a rational number of the specified double value.
	 * 
	 * @param value the double value
	 * @return the rational number
	 * @throws NumberFormatException if the double value is Infinite or NaN.
	 */
	public static BigRational valueOf(double value) {
		if (value == 0.0) {
			return ZERO;
		}
		if (value == 1.0) {
			return ONE;
		}
		if (Double.isInfinite(value)) {
			throw new NumberFormatException("Infinite");
		}
		if (Double.isNaN(value)) {
			throw new NumberFormatException("NaN");
		}
		return valueOf(new BigDecimal(String.valueOf(value)));
	}

	/**
	 * Creates a rational number of the specified {@link BigDecimal} value.
	 * 
	 * @param value the double value
	 * @return the rational number
	 */
	public static BigRational valueOf(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) == 0) {
			return ZERO;
		}
		if (value.compareTo(BigDecimal.ONE) == 0) {
			return ONE;
		}
		if (value.scale() < 0) {
			BigDecimal n = new BigDecimal(value.unscaledValue().multiply(BigInteger.TEN.pow(-value.scale())));
			return new BigRational(n, BigDecimal.ONE);
		}
		else {
			BigDecimal n = new BigDecimal(value.unscaledValue());
			BigDecimal d = new BigDecimal(BigInteger.TEN.pow(value.scale()));
			return new BigRational(n, d);
		}
	}

	/**
	 * Creates a rational number of the specified string representation.
	 * 
	 * <p>The accepted string representations are:
	 * <ul>
	 * <li>Output of {@link BigRational#toString()} : "integerPart.fractionPart"</li>
	 * <li>Output of {@link BigRational#toRationalString()} : "numerator/denominator"</li>
	 * <li>Output of <code>toString()</code> of {@link BigDecimal}, {@link BigInteger}, {@link Integer}, ...</li>
	 * <li>Output of <code>toString()</code> of {@link Double}, {@link Float} - except "Infinity", "-Infinity" and "NaN"</li>
	 * </ul>
	 * </p>
	 * 
	 * @param string the string representation to convert
	 * @return the rational number
	 * @throws ArithmeticException if the denominator is 0 (division by zero)
	 */
	public static BigRational valueOf(String string) {
		String[] strings = string.split("/");
		BigRational result = valueOfSimple(strings[0]);
		for (int i = 1; i < strings.length; i++) {
			result = result.divide(valueOfSimple(strings[i]));
		}
		return result;
	}

	private static BigRational valueOfSimple(String string) {
		return valueOf(new BigDecimal(string));
	}

	public static BigRational valueOfSimple2(String string) {
		boolean positive = true;
		String integerPart = "";
		String fractionPart = "";
		String fractionRepeatPart = "";
		String exponentPart = "";

		int index = 0;
		if (string.charAt(index) == '+') {
			index++;
		}
		else if (string.charAt(index) == '-') {
			index++;
			positive = false;
		}

		int pointPos = string.indexOf('.', index);
		if (pointPos >= 0) {
			integerPart = string.substring(index, pointPos);
			index = pointPos;
		}

		int repeatPos = string.indexOf('[', index);
		int exponentPos = string.indexOf('E', index);

		if (repeatPos >= 0) {
			fractionPart = string.substring(index, repeatPos);

			int repeatEndPos = string.indexOf(']', index);
			if (repeatEndPos == -1) {
				repeatEndPos = string.length();
			}
			fractionRepeatPart = string.substring(repeatPos, repeatEndPos);
			index = repeatEndPos;
		}
		else {
			if (exponentPos >= 0) {
				fractionPart = string.substring(repeatPos, exponentPos);
			}
			else {
				fractionPart = string.substring(index);
			}
		}

		if (exponentPos >= 0) {
			exponentPart = string.substring(exponentPos);
		}

		return valueOf(positive, integerPart, fractionPart, fractionRepeatPart, exponentPart);
	}

	public static BigRational valueOfSimple3(String string) {
		char[] chars = string.toCharArray();
		int index = 0;
		final int INIT = 0;
		final int INT_PART = 1;
		final int FRACT_PART = 2;
		final int FRACT_REPEAT_PART = 3;
		final int EXP_REPEAT_PART = 4;
		int state = INIT;

		boolean positive = true;

		while (index < chars.length) {
			char c = chars[index];
			switch (state) {
				case INIT:
					if (c == '+') {
						// ignore
						index++;
					}
					else if (c == '-') {
						positive = !positive;
						index++;
					}
					break;
			}
		}

		return null;
	}

	public static BigRational valueOf(boolean positive, String integerPart, String fractionPart, String fractionRepeatPart, String exponentPart) {
		BigRational result = ZERO;

		if (fractionRepeatPart != null && fractionRepeatPart.length() > 0) {
			BigInteger lotsOfNines = BigInteger.TEN.pow(fractionRepeatPart.length()).subtract(BigInteger.ONE);
			result = valueOf(new BigInteger(fractionRepeatPart), lotsOfNines);
		}

		if (fractionPart != null && fractionPart.length() > 0) {
			result = result.add(valueOf(new BigInteger(fractionPart)));
			result = result.divide(BigInteger.TEN.pow(fractionPart.length()));
		}

		if (integerPart != null && integerPart.length() > 0) {
			result = result.add(new BigInteger(integerPart));
		}

		if (exponentPart != null && exponentPart.length() > 0) {
			int exponent = Integer.parseInt(exponentPart);
			BigInteger powerOfTen = BigInteger.TEN.pow(Math.abs(exponent));
			result = exponent >= 0 ? result.multiply(powerOfTen) : result.divide(powerOfTen);
		}

		if (!positive) {
			result = result.negate();
		}

		return result;
	}

	private static BigRational valueOf(BigDecimal numerator, BigDecimal denominator) {
		if (numerator.signum() == 0 && denominator.signum() != 0) {
			return ZERO;
		}
		if (numerator.compareTo(BigDecimal.ONE) == 0 && denominator.compareTo(BigDecimal.ONE) == 0) {
			return ONE;
		}
		return new BigRational(numerator, denominator);
	}

	/**
	 * Returns the smallest of the specified rational numbers.
	 * 
	 * @param values the rational numbers to compare
	 * @return the smallest rational number, 0 if no numbers are specified
	 */
	public static BigRational min(BigRational... values) {
		if (values.length == 0) {
			return BigRational.ZERO;
		}
		BigRational result = values[0];
		for (int i = 1; i < values.length; i++) {
			result = result.min(values[i]);
		}
		return result;
	}

	/**
	 * Returns the largest of the specified rational numbers.
	 * 
	 * @param values the rational numbers to compare
	 * @return the largest rational number, 0 if no numbers are specified
	 * @see #max(BigRational)
	 */
	public static BigRational max(BigRational... values) {
		if (values.length == 0) {
			return BigRational.ZERO;
		}
		BigRational result = values[0];
		for (int i = 1; i < values.length; i++) {
			result = result.max(values[i]);
		}
		return result;
	}

	/**
	 * Calculates the factorial (n * (n-1) * (n-2) * ... * 1) of the specified int value.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param n the int value to calculate the factorial of
	 * @return the resulting rational number
	 */
	public static BigRational factorial(int n) {
		if (n < 0) {
			// TODO document
			throw new ArithmeticException("Negative value");
		}
		if (n < factorialCache.length) {
			return factorialCache[n];
		}

		BigRational result = factorialCache[factorialCache.length - 1];
		for (int i = factorialCache.length; i <= n; i++) {
			result = result.multiply(valueOf(i));
		}
		return result;
	}

	public static BigRational bernoulli(int n) {
		if (n < 0) {
			// TODO document
			throw new ArithmeticException("Negative value");
		}
		if (n == 1) {
			return valueOf(1, 2);
		}
		if (n % 2 == 1) {
			return ZERO;
		}
		return bernoulliCache[n / 2];
	}

	/**
	 * Calculates the square root of a rational number.
	 * 
	 * <p><a href="http://en.wikipedia.org/wiki/Square_root">Wikipedia: Square root</a></p>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses <a href="http://en.wikipedia.org/wiki/Newton%27s_method">Newtown's method</a>
	 * until the delta step is smaller than the specified scale (10<sup>-scale</sup>).</p>
	 *
	 * @param x the rational number to calculate the square root for
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated square root of x
	 */
	public static BigRational sqrt(BigRational x, int scale) {
		if (x.isZero()) {
			return ZERO;
		}
		
		final BigRational accuracy = convertScaleToAccuracy(scale);

		BigRational last = ZERO;
		BigRational result = x.divide(TWO);

		do {
			last = result;
			result = x.divide(result).add(last).divide(TWO);
		} while (last.subtract(result).abs().compareTo(accuracy) >= 0);
		return result.withScale(scale);
	}

	/**
	 * Calculates the natural logarithm of a rational number.
	 * 
	 * <a href="http://en.wikipedia.org/wiki/Natural_logarithm">Wikipedia: Natural logarithm</a>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses <a href="http://en.wikipedia.org/wiki/Taylor_series">Taylor series</a>
	 * until the delta step is smaller than the specified scale (10<sup>-scale</sup>).</p>
	 * 
	 * <p>For x < 1 the following series is used:</br>
	 * <code>sum(-1^(n+1)(x-1)^n/n)</code></p>
	 * <p>For x >= 1 the following series is used:</br>
	 * <code>sum(((x-1)/n)^n/n)</code></p>
	 *
	 * @param x the rational number to calculate the natural logarithm for
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the resulting rational number
	 * @throws ArithmeticException for 0 or negative numbers
	 */
	public static BigRational log(BigRational x, int scale) {
		// http://en.wikipedia.org/wiki/Natural_logarithm
		if (x.signum() <= 0) {
			throw new ArithmeticException("Log 0");
		}
		if (x.isOne()) {
			return ZERO;
		}
		return logAreaHyperbolicTangent(x, scale);
	}

	private static BigRational logAreaHyperbolicTangent(BigRational x, int scale) {
		// http://en.wikipedia.org/wiki/Logarithm#Calculation
		final BigRational accuracy = convertScaleToAccuracy(scale);
		
		BigRational magic = x.subtract(ONE).divide(x.add(ONE));
		
		BigRational result = ZERO;
		BigRational step;
		int i = 0;
		do {
			BigRational doubleIndexPlusOne = valueOf(i).multiply(2).add(ONE); 
			step = magic.pow(doubleIndexPlusOne, scale).divide(doubleIndexPlusOne);
			result = result.add(step);
			i++;
		} while (step.abs().compareTo(accuracy) >= 0);
		
		result = result.multiply(2);
		
		return result.withScale(scale);
	}

	/**
	 * Calculates the exponent of a rational number.
	 * 
	 * <p><a href="http://en.wikipedia.org/wiki/Exponent">Wikipedia: Exponent</a></p>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses <a href="http://en.wikipedia.org/wiki/Taylor_series">Taylor series</a>
	 * until the delta step is smaller than the specified scale (10<sup>-scale</sup>).</p>
	 *
	 * @param x the rational number to calculate the exponent for
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated exponent as rational number
	 */
	public static BigRational exp(BigRational x, int scale) {
		final BigRational accuracy = convertScaleToAccuracy(scale);

		BigRational result = ZERO;
		BigRational step;
		int i = 0;
		do {
			step = x.pow(i).divide(factorial(i));
			result = result.add(step);
			i++;
		} while (step.abs().compareTo(accuracy) >= 0);

		return result.withScale(scale);
	}

	/**
	 * Calculates the sine (sinus) of a rational number.
	 * 
	 * <p><a href="http://en.wikipedia.org/wiki/Sine">Wikipedia: Sine</a></p>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses <a href="http://en.wikipedia.org/wiki/Taylor_series">Taylor series</a>
	 * until the delta step is smaller than the specified scale (10<sup>-scale</sup>).</p>
	 *
	 * @param x the rational number to calculate the sine for
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated sine as rational number
	 */
	public static BigRational sin(BigRational x, int scale) {
		final BigRational accuracy = valueOf(10).pow(-scale - 1);

		BigRational result = ZERO;
		BigRational sign = ONE;
		BigRational step;
		int i = 0;
		do {
			step = sign.multiply(x.pow(2 * i + 1)).divide(factorial(2 * i + 1));
			sign = sign.negate();
			result = result.add(step);
			i++;
		} while (step.abs().compareTo(accuracy) >= 0);

		return result.withScale(scale);
	}

	/**
	 * Calculates the cosine (cosinus) of a rational number.
	 * 
	 * <p><a href="http://en.wikipedia.org/wiki/Cosine">Wikipedia: Cosine</a></p>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses <a href="http://en.wikipedia.org/wiki/Taylor_series">Taylor series</a>
	 * until the delta step is smaller than the specified scale (10<sup>-scale</sup>).</p>
	 *
	 * @param x the rational number to calculate the cosine for
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated cosine as rational number
	 */
	public static BigRational cos(BigRational x, int scale) {
		final BigRational accuracy = valueOf(10).pow(-scale - 1);

		BigRational result = ZERO;
		BigRational sign = ONE;
		BigRational step;
		int i = 0;
		do {
			step = sign.multiply(x.pow(2 * i)).divide(factorial(2 * i));
			sign = sign.negate();
			result = result.add(step);
			i++;
		} while (step.abs().compareTo(accuracy) >= 0);

		return result.withScale(scale);
	}

	/**
	 * Calculates the value of pi.
	 * 
	 * <p><a href="http://en.wikipedia.org/wiki/Pi">Wikipedia: Pi</a></p>
	 * 
	 * <p>The result has loss of precision, the desired precision must be specified by the <code>scale</code> argument.</p>
	 * 
	 * <p>The implementation uses the <a href="http://www.craig-wood.com/nick/articles/pi-chudnovsky/">Chudnovsky series</a></p>
	 *
	 * @param scale the scale (number of digits after the decimal point) of the calculated result
	 * @return the calculated value of pi as rational number
	 */
	public static BigRational pi(int scale) {
		BigDecimal value24 = BigDecimal.valueOf(24);
		BigRational value640320 = BigRational.valueOf(640320);
		BigDecimal value13591409 = BigDecimal.valueOf(13591409);
		BigDecimal value545140134 = BigDecimal.valueOf(545140134);
		BigRational valueDivisor = value640320.pow(3).divide(value24);

		BigRational sumA = BigRational.ONE;
		BigRational sumB = BigRational.ZERO;

		BigRational a = BigRational.ONE;
		long dividendTerm1 = 5; // -(6*k - 5)
		long dividendTerm2 = -1; // 2*k - 1
		long dividendTerm3 = -1; // 6*k - 1

		long iterationCount = (scale+13) / 14;
		for (long k = 1; k <= iterationCount; k++) {
			BigDecimal valueK = BigDecimal.valueOf(k);
			dividendTerm1 += -6;
			dividendTerm2 += 2;
			dividendTerm3 += 6;
			BigDecimal dividend = BigDecimal.valueOf(dividendTerm1).multiply(BigDecimal.valueOf(dividendTerm2)).multiply(BigDecimal.valueOf(dividendTerm3));
			BigRational divisor = valueDivisor.multiply(valueK.pow(3));
			a = a.multiply(dividend).divide(divisor).reduce();
			BigRational b = a.multiply(valueK);
			
			sumA = sumA.add(a);
			sumB = sumB.add(b);
		}
		
		BigDecimal value426880 = BigDecimal.valueOf(426880);
		BigRational value10005 = BigRational.valueOf(10005);
		BigRational factor = sqrt(value10005, scale+10).multiply(value426880);
		BigRational pi = factor.divide(sumA.multiply(value13591409).add(sumB.multiply(value545140134)));
		return pi.withScale(scale);
	}

	private static BigRational convertScaleToAccuracy(int scale) {
		return TEN.pow(-scale - 2);
	}

	/**
	 * A context for rational calculations with a specific scale.
	 */
	public static class Context {
		private int scale;

		/**
		 * Creates context with the specified scale.
		 * 
		 * @param scale the scale to be used for the calculations with this context
		 */
		public Context(int scale) {
			this.scale = scale;
		}

		/**
		 * Returns the scale of this context.
		 * 
		 * @return the scale
		 */
		public int getScale() {
			return scale;
		}

		/**
		 * Calculates pi with the scale of this context.
		 * 
		 * @return the value of pi
		 * @see BigRational#pi(int)
		 */
		public BigRational pi() {
			return BigRational.pi(scale);
		}

		/**
		 * Calculates the square root of a rational number with the scale of this context.
		 * 
		 * @param x the rational number to calculate the square root for
		 * @return the calculated square root of x
		 * @see BigRational#sqrt(BigRational, int)
		 */
		public BigRational sqrt(BigRational x) {
			return BigRational.sqrt(x, scale);
		}

		/**
		 * Calculates x to the power of y (x<sup>y</sup>) with the scale of this context.
		 * @param x the base rational number
		 * @param y the exponent rational number 
		 * @return the calculated power
		 * @see BigRational#pow(BigRational, int)
		 */
		public BigRational pow(BigRational x, BigRational y) {
			return x.pow(y, scale);
		}

		/**
		 * Calculates the exponent of a rational number with the scale of this context.
		 * 
		 * @param x the rational number to calculate the exponent for
		 * @return the calculated exponent of x
		 * @see BigRational#exp(BigRational, int)
		 */
		public BigRational exp(BigRational x) {
			return BigRational.exp(x, scale);
		}

		/**
		 * Calculates the natural logarithm of a rational number with the scale of this context.
		 * 
		 * @param x the rational number to calculate the natural logarithm for
		 * @return the calculated natural logarithm of x
		 * @see BigRational#log(BigRational, int)
		 */
		public BigRational log(BigRational x) {
			return BigRational.log(x, scale);
		}

		/**
		 * Calculates the sinus of a rational number with the scale of this context.
		 * 
		 * @param x the rational number to calculate the sinus for
		 * @return the calculated sinus of x
		 * @see BigRational#sin(BigRational, int)
		 */
		public BigRational sin(BigRational x) {
			return BigRational.sin(x, scale);
		}

		/**
		 * Calculates the cosinus of a rational number with the scale of this context.
		 * 
		 * @param x the rational number to calculate the cosinus for
		 * @return the calculated cosinus of x
		 * @see BigRational#cos(BigRational, int)
		 */
		public BigRational cos(BigRational x) {
			return BigRational.cos(x, scale);
		}
	}
}
