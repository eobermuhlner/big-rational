package ch.obermuhlner.math.rational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static ch.obermuhlner.math.rational.BigRational.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.junit.Test;

import ch.obermuhlner.math.rational.BigRational.Context;

/**
 * Tests {@link BigRational}.
 */
public class BigRationalTest {

	private static final String PI_STRING = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651";
//								  Pi(1000) = 3.141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117067982148086513282306647093844609550582231725359408128481117450284102701938521105559644622948954930381964428810975665933446128475648233786783165271201909145648566923460348610454326648213393607260249141273724587006606315588174881520920962829254091715364367892590360011330530548820466521384146951941511609433057270365759591953092186117381932611793105118548074462379962749567351885752724891227938183011949129833673362440656643086021394946395224737190702179860943702770539217176293176752384674818467669405132000568127145263560827785771342757789609173637178721468440901224953430146549585371050792279689258923542019956112129021960864034418159813629774771309960518707211349999998372978049951059731732816096318595024459455346908302642522308253344685035261931188171010003137838752886587533208381420617177669147303598253490428755468731159562863882353787593751957781857780532171226806613001927876611195909216420199
	private static final double DOUBLE_ACCURACY = 0.00000001;
	private static final int SCALE_FOR_DOUBLE_COMPARISON = 15;

	/**
	 * Tests {@link BigRational#valueOf(int)}.
	 */
	@Test
	public void testValueOfInt() {
		assertSame(ZERO, valueOf(0));
		assertSame(ONE, valueOf(1));

		assertEquals("0", valueOf(0).toString());
		assertEquals("123", valueOf(123).toString());
		assertEquals("-123", valueOf(-123).toString());
	}

	/**
	 * Tests {@link BigRational#valueOf(int, int)}.
	 */
	@Test
	public void testValueOfRationalInt() {
		assertSame(ZERO, valueOf(0, 1));
		assertSame(ZERO, valueOf(0, 2));
		assertSame(ZERO, valueOf(0, -3));
		assertSame(ONE, valueOf(1,1));
		assertSame(ONE, valueOf(2,2).reduce()); // needs reduce

		assertEquals("0.5", valueOf(1, 2).toString());
		assertEquals(BigInteger.valueOf(1), valueOf(1, 2).getNumerator());
		assertEquals(BigInteger.valueOf(2), valueOf(1, 2).getDenominator());

		assertEquals("1/2", valueOf(1, 2).toRationalString());
		assertEquals("2/4", valueOf(2, 4).toRationalString());
		assertEquals("1/2", valueOf(2, 4).reduce().toRationalString()); // needs reduce
	}

	/**
	 * Tests {@link BigRational#valueOf(int, int, int)}.
	 */
	@Test
	public void testValueOfIntegerRationalInt() {
		assertSame(ZERO, valueOf(0, 0, 1));
		
		assertEquals("3.5", valueOf(3, 1, 2).toString());
		assertEquals("-3.5", valueOf(-3, 1, 2).toString());
	}

	/**
	 * Tests {@link BigRational#valueOf(int, int, int)} with 0 denominator.
	 */
	@Test(expected = ArithmeticException.class)
	public void testValueOfIntegerRationalIntDenominator0() {
		valueOf(1, 2, 0);
	}
	
	/**
	 * Tests {@link BigRational#valueOf(int, int, int)} with all arguments 0 (including denominator).
	 */
	@Test(expected = ArithmeticException.class)
	public void testValueOfIntegerRationalIntAll0() {
		valueOf(0, 0, 0);
	}

	/**
	 * Tests {@link BigRational#valueOf(int, int, int)} with negative numerator.
	 */
	@Test(expected = ArithmeticException.class)
	public void testValueOfIntegerRationalIntNegativeFractionNumerator() {
		valueOf(1, -2, 3);
	}

	/**
	 * Tests {@link BigRational#valueOf(int, int, int)} with negative denominator.
	 */
	@Test(expected = ArithmeticException.class)
	public void testValueOfIntegerRationalIntNegativeFractionDenominator() {
		valueOf(1, 2, -3);
	}

	/**
	 * Tests {@link BigRational#valueOf(BigInteger, BigInteger)}.
	 */
	@Test
	public void testValueOfRationalBigInteger() {
		assertSame(ZERO, valueOf(BigInteger.ZERO, BigInteger.ONE));
		assertSame(ZERO, valueOf(BigInteger.ZERO, BigInteger.valueOf(2)));
		assertSame(ZERO, valueOf(BigInteger.ZERO, BigInteger.valueOf(-3)));
		assertSame(ONE, valueOf(BigInteger.ONE, BigInteger.ONE));
		assertSame(ONE, valueOf(BigInteger.TEN, BigInteger.TEN).reduce()); // needs reduce

		assertEquals("1/10", valueOf(BigInteger.ONE, BigInteger.TEN).toRationalString());
	}
	
	/**
	 * Tests {@link BigRational#valueOf(int, int)} with second argument 0. 
	 */
	@Test(expected = ArithmeticException.class)
	public void testValueOfRationalIntDivideByZero() {
		valueOf(3, 0);
	}

	/**
	 * Tests {@link BigRational#valueOf(double)}.
	 */
	@Test
	public void testValueOfDouble() {
		assertSame(ZERO, valueOf(0.0));
		assertSame(ONE, valueOf(1.0));

		assertEquals("0", valueOf(0.0).toString());
		assertEquals("123", valueOf(123).toString());
		assertEquals("-123", valueOf(-123).toString());
		assertEquals("123.456", valueOf(123.456).toString());
		assertEquals("-123.456", valueOf(-123.456).toString());
	}

	/**
	 * Tests {@link BigRational#valueOf(double)} with {@link Double#POSITIVE_INFINITY}.
	 */
	@Test(expected=NumberFormatException.class)
	public void testValueOfDoublePositiveInfinity() {
		valueOf(Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Tests {@link BigRational#valueOf(double)} with {@link Double#NEGATIVE_INFINITY}.
	 */
	@Test(expected=NumberFormatException.class)
	public void testValueOfDoubleNegativeInfinity() {
		valueOf(Double.NEGATIVE_INFINITY);
	}
	
	/**
	 * Tests {@link BigRational#valueOf(double)} with {@link Double#NaN}.
	 */
	@Test(expected=NumberFormatException.class)
	public void testValueOfDoubleNaN() {
		valueOf(Double.NaN);
	}
	
	/**
	 * Tests {@link BigRational#valueOf(BigInteger)}.
	 */
	@Test
	public void testValueOfBigInteger() {
		assertSame(ZERO, valueOf(BigInteger.ZERO));
		assertSame(ONE, valueOf(BigInteger.ONE));

		assertEquals("0", valueOf(BigInteger.ZERO).toString());
		assertEquals("123", valueOf(BigInteger.valueOf(123)).toString());
		assertEquals("-123", valueOf(BigInteger.valueOf(-123)).toString());
	}

	/**
	 * Tests {@link BigRational#valueOf(BigDecimal)}.
	 */
	@Test
	public void testValueOfBigDecimal() {
		assertSame(ZERO, valueOf(BigDecimal.ZERO));
		assertSame(ONE, valueOf(BigDecimal.ONE));

		assertEquals("0", valueOf(new BigDecimal("0")).toString());
		assertEquals("123", valueOf(new BigDecimal("123")).toString());
		assertEquals("-123", valueOf(new BigDecimal("-123")).toString());
		assertEquals("123.456", valueOf(new BigDecimal("123.456")).toString());
		assertEquals("-123.456", valueOf(new BigDecimal("-123.456")).toString());
	}

	/**
	 * Tests {@link BigRational#valueOf(String)}.
	 */
	@Test
	public void testValueOfString() {
		assertSame(ZERO, valueOf("0"));
		assertSame(ONE, valueOf("1"));
		assertSame(ZERO, valueOf("0.0"));
		assertSame(ZERO, valueOf("0/1"));
		assertSame(ZERO, valueOf("0/2"));

		assertEquals("123", valueOf("123").toString());
		assertEquals("123.456", valueOf("123.456").toString());

		assertEquals("-123", valueOf("-246/2").toString());
		assertEquals("-1234.56", valueOf("123.456/-0.1").toString());
		assertEquals("123456", valueOf("1.23456E5").toString());
		assertEquals("-123456", valueOf("-1.23456E5").toString());
		assertEquals("12300000", valueOf("123E5").toString());
		assertEquals("-12300000", valueOf("-123E5").toString());

		//assertEquals("X", valueOfSimple2("123").toString());
	}

	@Test
	public void testValueOfString3() {
		assertSame(ZERO, valueOf(true, null, null, null, null));
		assertSame(ZERO, valueOf(true, "", "", "", ""));
		assertSame(ZERO, valueOf(true, "0", "", "", ""));
		assertSame(ZERO, valueOf(true, "0", "0", "0", "0"));

		assertEquals("0.456", valueOf(true, "0", "456", "", "").toString());
		assertEquals("123.45", valueOf(true, "123", "45", "", "").toString());
		assertEquals("1/3", valueOf(true, "", "", "3", "").reduce().toRationalString());
		assertEquals("4/3", valueOf(true, "1", "", "3", "").reduce().toRationalString());
		assertEquals("37/30", valueOf(true, "1", "2", "3", "").reduce().toRationalString());

		assertEquals("-123.45", valueOf(false, "123", "45", "", "").toString());

		assertEquals("123450", valueOf(true, "123", "45", "", "3").toString());
		assertEquals("1.2345", valueOf(true, "123", "45", "", "-2").toString());
	}

	/**
	 * Tests {@link BigRational#isZero()}.
	 */
	@Test
	public void testIsZero() {
		assertEquals(true, valueOf(0).isZero());
		
		assertEquals(false, valueOf(1).isZero());
		assertEquals(false, valueOf(0.5).isZero());
		assertEquals(false, valueOf(-1).isZero());
		assertEquals(false, valueOf(-0.5).isZero());
	}
	
	/**
	 * Tests {@link BigRational#isInteger()}.
	 */
	@Test
	public void testIsInteger() {
		assertEquals(true, valueOf(0).isInteger());
		assertEquals(true, valueOf(1).isInteger());
		assertEquals(true, valueOf(-1).isInteger());
		assertEquals(true, valueOf(4, 4).isInteger());
		assertEquals(true, valueOf(4, 2).isInteger());
		
		assertEquals(false, valueOf(0.5).isInteger());
		assertEquals(false, valueOf(-0.5).isInteger());
	}
	
	/**
	 * Tests {@link BigRational#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("0", valueOf(0).toString());
		assertEquals("123", valueOf(123).toString());
		assertEquals("0.25", valueOf(1, 4).toString());
		assertEquals("2", valueOf(4, 2).toString());
		assertEquals("-0.25", valueOf(-1, 4).toString());
		assertEquals("-2", valueOf(-4, 2).toString());
	}
	
	/**
	 * Tests {@link BigRational#toRationalString()}.
	 */
	@Test
	public void testToRationalString() {
		assertEquals("0", valueOf(0).toRationalString());
		assertEquals("123", valueOf(123).toRationalString());
		assertEquals("2/3", valueOf(2, 3).toRationalString());
		assertEquals("-2/3", valueOf(-2, 3).toRationalString());
		assertEquals("-2/3", valueOf(2, -3).toRationalString());
		
		assertEquals("4/4", valueOf(4, 4).toRationalString()); // not reduced
	}
	
	/**
	 * Tests {@link BigRational#toRationalString()}.
	 */
	@Test
	public void testToIntegerRationalString() {
		assertEquals("0", valueOf(0).toIntegerRationalString());
		assertEquals("1", valueOf(1).toIntegerRationalString());
		
		assertEquals("1/2", valueOf(1, 2).toIntegerRationalString());
		
		assertEquals("1 2/3", valueOf(1, 2, 3).toIntegerRationalString());
		assertEquals("-1 2/3", valueOf(-1, 2, 3).toIntegerRationalString());
	}
	
	/**
	 * Tests {@link BigRational#toDouble()}.
	 */
	@Test
	public void testToDouble() {
		assertEquals(0.0, valueOf(0).toDouble(), 0.0);
		assertEquals(123.0, valueOf(123.0).toDouble(), 0.0);
		assertEquals(123.4, valueOf(123.4).toDouble(), 0.0);
		assertEquals(-123.0, valueOf(-123.0).toDouble(), 0.0);
		assertEquals(-123.4, valueOf(-123.4).toDouble(), 0.0);
	}
	
	/**
	 * Tests {@link BigRational#toFloat()}.
	 */
	@Test
	public void testToFloat() {
		assertEquals(0.0f, valueOf(0).toFloat(), 0.0);
		assertEquals(123.0f, valueOf(123.0).toFloat(), 0.0);
		assertEquals(123.4f, valueOf(123.4).toFloat(), 0.0);
		assertEquals(-123.0f, valueOf(-123.0).toFloat(), 0.0);
		assertEquals(-123.4f, valueOf(-123.4).toFloat(), 0.0);
	}
	
	/**
	 * Tests {@link BigRational#integerPart()}.
	 */
	@Test
	public void testIntegerPart() {
		assertEquals("0", valueOf(2, 3).integerPart().toString());
		assertEquals("1", valueOf(4, 3).integerPart().toString());

		assertEquals("0", valueOf(-2, 3).integerPart().toString());
		assertEquals("-1", valueOf(-4, 3).integerPart().toString());
	}

	/**
	 * Tests {@link BigRational#fractionPart()}.
	 */
	@Test
	public void testFractionPart() {
		assertEquals("2/3", valueOf(2, 3).fractionPart().toRationalString());
		assertEquals("1/3", valueOf(4, 3).fractionPart().toRationalString());

		assertEquals("-2/3", valueOf(-2, 3).fractionPart().toRationalString());
		assertEquals("-1/3", valueOf(-4, 3).fractionPart().toRationalString());
	}

	/**
	 * Tests {@link BigRational#withPrecision(int)}.
	 */
	@Test
	public void testWithPrecision() {
		assertEquals("123.456", valueOf(123.456).withPrecision(7).toString()); // unchanged
		assertEquals("123.456", valueOf(123.456).withPrecision(6).toString());
		assertEquals("123.46", valueOf(123.456).withPrecision(5).toString()); // rounding up
		assertEquals("123.5", valueOf(123.456).withPrecision(4).toString()); // rounding up
		assertEquals("123", valueOf(123.456).withPrecision(3).toString());
		assertEquals("120", valueOf(123.456).withPrecision(2).toString());
		assertEquals("100", valueOf(123.456).withPrecision(1).toString());
		
		assertEquals("123.456", valueOf(123.456).withPrecision(0).toString()); // unchanged
	}

	/**
	 * Tests {@link BigRational#withPrecision(int)} with negative precision.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testWithPrecisionIllegalPrecision() {
		assertEquals("123.456", valueOf(123.456).withPrecision(-1).toString());		
	}

	/**
	 * Tests {@link BigRational#withScale(int)}.
	 */
	@Test
	public void testWithScale() {
		assertEquals("123.456", valueOf(123.456).withScale(4).toString()); // unchanged
		assertEquals("123.456", valueOf(123.456).withScale(3).toString());
		assertEquals("123.46", valueOf(123.456).withScale(2).toString()); // rounding up
		assertEquals("123.5", valueOf(123.456).withScale(1).toString()); // rounding up
		assertEquals("123", valueOf(123.456).withScale(0).toString());
		assertEquals("120", valueOf(123.456).withScale(-1).toString());
		assertEquals("100", valueOf(123.456).withScale(-2).toString());
		assertEquals("0", valueOf(123.456).withScale(-3).toString());
		
		BigDecimal bigDecimalTestValue = new BigDecimal(PI_STRING);
		BigRational bigRationalTestValue = BigRational.valueOf(PI_STRING);
		for (int i = 0; i < 20; i++) {
			String referenceString = bigDecimalTestValue.setScale(i, RoundingMode.HALF_UP).toString();
			referenceString = referenceString.replaceAll("0+$", ""); // remove trailing '0'
			assertEquals("i="+i, referenceString, bigRationalTestValue.withScale(i).toString());
		}		
	}

	/**
	 * Tests {@link BigRational#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		assertTrue(ZERO.equals(ZERO));
		assertTrue(ZERO.equals(valueOf(0, 99)));
		assertTrue(valueOf(33).equals(valueOf(33)));
		assertTrue(valueOf(1, 3).equals(valueOf(1, 3)));
		assertTrue(valueOf(-1, 3).equals(valueOf(1, -3)));

		assertFalse(ZERO.equals(null));
		assertFalse(ZERO.equals("string"));
		assertFalse(ZERO.equals(ONE));
		assertFalse(valueOf(1, 3).equals(valueOf(1, 4)));
	}

	/**
	 * Tests {@link BigRational#compareTo(BigRational)}.
	 */
	@Test
	public void testCompareTo() {
		assertEquals(0, ZERO.compareTo(ZERO));
		
		assertEquals(0, valueOf(1, 3).compareTo(valueOf(1, 3)));
		assertEquals(-1, valueOf(1, 4).compareTo(valueOf(1, 3)));
		assertEquals(1, valueOf(1, 2).compareTo(valueOf(1, 3)));
	}
	
	/**
	 * Tests {@link BigRational#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		// no asserts, since hashCode() defines no concrete values
		ZERO.hashCode();
		
		valueOf(1, 3).hashCode();
	}
	
	/**
	 * Tests {@link BigRational#min(BigRational...)}.
	 */
	@Test
	public void testMin() {
		assertEquals(ZERO, min());

		assertEquals(valueOf(3), min(valueOf(3)));
		assertEquals(valueOf(-2), min(valueOf(-2)));
		assertEquals(valueOf(-2), min(valueOf(3), valueOf(-2)));
		assertEquals(valueOf(-2), min(valueOf(-2), valueOf(3)));
	}

	/**
	 * Tests {@link BigRational#max(BigRational...)}.
	 */
	@Test
	public void testMax() {
		assertEquals(ZERO, max());

		assertEquals(valueOf(3), max(valueOf(3)));
		assertEquals(valueOf(-2), max(valueOf(-2)));
		assertEquals(valueOf(3), max(valueOf(3), valueOf(-2)));
		assertEquals(valueOf(3), max(valueOf(-2), valueOf(3)));
	}

	/**
	 * Tests that the same instance {@link BigRational#ZERO} is returned from operations with result 0.
	 */
	@Test
	public void testSameZERO() {
		assertSame(ZERO, ZERO.negate());
		assertSame(ZERO, ZERO.abs());
		assertSame(ZERO, ZERO.add(ZERO));
		assertSame(ZERO, ZERO.subtract(ZERO));
		assertSame(ZERO, ZERO.multiply(ONE));
		assertSame(ZERO, ZERO.divide(ONE));
	}

	/**
	 * Tests that the same instance {@link BigRational#ZERO} is returned from operations with result 1.
	 */
	@Test
	public void testSameONE() {
		assertSame(ONE, ONE.negate().negate());
		assertSame(ONE, ONE.abs());
		assertSame(ONE, ONE.negate().abs());
		assertSame(ONE, ONE.add(ZERO));
		assertSame(ONE, ZERO.add(ONE));
		assertSame(ONE, ONE.subtract(ZERO));
		assertSame(ONE, ONE.multiply(ONE));
		assertSame(ONE, ONE.divide(ONE));
		assertSame(ONE, valueOf(3).divide(valueOf(3)).reduce()); // needs reduce
	}

	/**
	 * Tests {@link BigRational#negate()}.
	 */
	@Test
	public void testNegate() {
		assertEquals("-2", valueOf(2).negate().toString());

		assertEquals("0.5", valueOf(-0.5).negate().toString());
	}

	/**
	 * Tests {@link BigRational#reciprocal()}.
	 */
	@Test
	public void testReciprocal() {
		assertEquals("0.5", valueOf(2).reciprocal().toString());

		assertEquals("-2", valueOf(-0.5).reciprocal().toString());
	}

	/**
	 * Tests {@link BigRational#reciprocal()} with 0.
	 */
	@Test(expected = ArithmeticException.class)
	public void testReciprocalZero() {
		ZERO.reciprocal();
	}

	/**
	 * Tests {@link BigRational#abs()}.
	 */
	@Test
	public void testAbs() {
		assertEquals("2", valueOf(2).abs().toString());
		assertEquals("0.5", valueOf(-0.5).abs().toString());
	}

	/**
	 * Tests {@link BigRational#abs()} optimization to return the same instance for positive values.
	 */
	@Test
	public void testAbsOptimized() {
		BigRational L1 = valueOf(2);
		assertSame(L1, L1.abs());
	}

	/**
	 * Tests {@link BigRational#signum()}.
	 */
	@Test
	public void testSignum() {
		assertSame(0, ZERO.signum());
		assertSame(-1, valueOf(-47).signum());
		assertSame(1, valueOf(99).signum());
	}

	/**
	 * Tests {@link BigRational#increment()}.
	 */
	@Test
	public void testIncrement() {
		assertEquals("5", valueOf(4).increment().toString());
		assertEquals("1.2", valueOf(2, 10).increment().toString());
	}

	/**
	 * Tests {@link BigRational#decrement()}.
	 */
	@Test
	public void testDecrement() {
		assertEquals("3", valueOf(4).decrement().toString());
		assertEquals("-0.8", valueOf(2, 10).decrement().toString());
	}

	/**
	 * Tests {@link BigRational#add(BigRational)}.
	 */
	@Test
	public void testAdd() {
		assertEquals("2", valueOf(2).add(valueOf(0)).toString());
		assertEquals("5", valueOf(2).add(valueOf(3)).toString());

		assertEquals("200.03", valueOf(200).add(valueOf(0.03)).toString());
	}

	/**
	 * Tests {@link BigRational#add(BigRational)} if the denominator is the same (should be optimized).
	 */
	@Test
	public void testAddOptimized() {
		assertEquals("3/7", valueOf(2, 7).add(valueOf(1, 7)).toRationalString());
	}

	/**
	 * Tests {@link BigRational#add(int)}.
	 */
	@Test
	public void testAddInt() {
		assertEquals("2", valueOf(2).add(0).toString());
		
		assertEquals("5", valueOf(2).add(3).toString());
	}

	/**
	 * Tests {@link BigRational#add(BigInteger)}.
	 */
	@Test
	public void testAddBigInteger() {
		assertEquals("2", valueOf(2).add(BigInteger.valueOf(0)).toString());
		
		assertEquals("5", valueOf(2).add(BigInteger.valueOf(3)).toString());
	}

	/**
	 * Tests {@link BigRational#subtract(BigRational)}.
	 */
	@Test
	public void testSubtract() {
		assertEquals("2", valueOf(2).subtract(valueOf(0)).toString());
		assertEquals("-1", valueOf(2).subtract(valueOf(3)).toString());

		assertEquals("199.97", valueOf(200).subtract(valueOf(0.03)).toString());
	}

	/**
	 * Tests {@link BigRational#subtract(BigRational)} if the denominator is the same (should be optimized).
	 */
	@Test
	public void testSubtractOptimized() {
		assertEquals("2/7", valueOf(3, 7).subtract(valueOf(1, 7)).toRationalString());
	}
	
	/**
	 * Tests {@link BigRational#subtract(int)}.
	 */
	@Test
	public void testSubtractInt() {
		assertEquals("5", valueOf(5).subtract(0).toString());
		
		assertEquals("2", valueOf(5).subtract(3).toString());
	}

	/**
	 * Tests {@link BigRational#subtract(BigInteger)}.
	 */
	@Test
	public void testSubtractBigInteger() {
		assertEquals("5", valueOf(5).subtract(BigInteger.valueOf(0)).toString());
		
		assertEquals("2", valueOf(5).subtract(BigInteger.valueOf(3)).toString());
	}

	/**
	 * Tests {@link BigRational#multiply(BigRational)}.
	 */
	@Test
	public void testMultiply() {
		assertEquals("6", valueOf(2).multiply(valueOf(3)).toString());
		assertEquals("6", valueOf(3).multiply(valueOf(2)).toString());

		assertEquals("60", valueOf(300).multiply(valueOf(0.2)).toString());
		assertEquals("60", valueOf(0.2).multiply(valueOf(300)).toString());

		assertEquals("0.06", valueOf(0.3).multiply(valueOf(0.2)).toString());
		assertEquals("0.06", valueOf(0.2).multiply(valueOf(0.3)).toString());
		
		assertEquals("-0.06", valueOf(-0.3).multiply(valueOf(0.2)).toString());
		assertEquals("-0.06", valueOf(0.3).multiply(valueOf(-0.2)).toString());
	}

	/**
	 * Tests {@link BigRational#multiply(BigRational)} with certain special cases (should be optimizied).
	 */
	@Test
	public void testMultiplyOptimized() {
		// multiply with 0
		assertEquals("0", valueOf(2).multiply(valueOf(0)).toString());
		assertEquals("0", valueOf(0).multiply(valueOf(2)).toString());
		
		// multiply with 1
		assertEquals("2", valueOf(2).multiply(valueOf(1)).toString());
		assertEquals("2", valueOf(1).multiply(valueOf(2)).toString());
	}

	/**
	 * Tests {@link BigRational#multiply(int)}.
	 */
	@Test
	public void testMultiplyInt() {
		assertEquals("0.6", valueOf(0.2).multiply(3).toString());
	}

	/**
	 * Tests {@link BigRational#multiply(BigInteger)}.
	 */
	@Test
	public void testMultiplyBigInteger() {
		assertEquals("0", valueOf(2).multiply(BigInteger.valueOf(0)).toString());
		assertEquals("2", valueOf(2).multiply(BigInteger.valueOf(1)).toString());
		assertEquals("2", valueOf(1).multiply(BigInteger.valueOf(2)).toString());
		assertEquals("0.6", valueOf(0.2).multiply(BigInteger.valueOf(3)).toString());
	}

	/**
	 * Tests {@link BigRational#divide(BigRational)}.
	 */
	@Test
	public void testDivide() {
		assertEquals("2", valueOf(6).divide(valueOf(3)).toString());

		assertEquals("25", valueOf(5).divide(valueOf(0.2)).toString());
	}

	/**
	 * Tests {@link BigRational#divide(int)}.
	 */
	@Test
	public void testDivideInt() {
		assertEquals("2", valueOf(6).divide(3).toString());
	}

	/**
	 * Tests {@link BigRational#divide(BigInteger)}.
	 */
	@Test
	public void testDivideBigInteger() {
		assertEquals("6", valueOf(6).divide(BigInteger.valueOf(1)).toString());

		assertEquals("2", valueOf(6).divide(BigInteger.valueOf(3)).toString());
	}

	/**
	 * Tests {@link BigRational#divide(BigRational)} with 0.
	 */
	@Test(expected = ArithmeticException.class)
	public void testDivideByZero() {
		ONE.divide(ZERO);
	}

	/**
	 * Tests {@link BigRational#pow(int)}.
	 */
	@Test
	public void testPowInt() {
		assertEquals(String.valueOf((int) Math.pow(2, 3)), valueOf(2).pow(3).toString());
		assertEquals(String.valueOf((int) Math.pow(-2, 3)), valueOf(-2).pow(3).toString());

		assertEquals("1000", valueOf(10).pow(3).toString());
		assertEquals("0.001", valueOf(10).pow(-3).toString());

		BigRational L1 = valueOf(0.02);
		assertEquals(L1.multiply(L1).multiply(L1), L1.pow(3));
	}

	/**
	 * Tests {@link BigRational#pow(BigRational, int)}.
	 */
	@Test
	public void testPowBigRational() {
		final double x = 2.1;
		for (int i = 0; i < 10; i++) {
			assertEquals(Math.pow(x, i), valueOf(x).pow(valueOf(i), SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}
	
	/**
	 * Tests {@link BigRational#pow(BigRational, int)} with integer arguments.
	 * Same tests as in {@link #testPowInt()}, but with {@link BigRational} integer arguments. 
	 */
	@Test
	public void testPowBigRationalOptimized() {
		final int precision = 3;
		
		assertEquals(String.valueOf((int) Math.pow(2, 3)), valueOf(2).pow(valueOf(3), precision).toString());
		assertEquals(String.valueOf((int) Math.pow(-2, 3)), valueOf(-2).pow(valueOf(3), precision).toString());

		assertEquals("1000", valueOf(10).pow(valueOf(3), precision).toString());
		assertEquals("0.001", valueOf(10).pow(valueOf(-3), precision).toString());

		BigRational L1 = valueOf(0.02);
		assertEquals(L1.multiply(L1).multiply(L1), L1.pow(valueOf(3), precision));
	}
	
	/**
	 * Tests {@link BigRational#factorial(int)}.
	 */
	@Test
	public void testFactorial() {
		assertEquals("1", factorial(0).toString());
		assertEquals("1", factorial(1).toString());
		assertEquals("2", factorial(2).toString());
		assertEquals("6", factorial(3).toString());
		assertEquals("24", factorial(4).toString());
		assertEquals("120", factorial(5).toString());
		
		assertEquals("9425947759838359420851623124482936749562312794702543768327889353416977599316221476503087861591808346911623490003549599583369706302603264000000000000000000000000", factorial(101).toString());
	}

	/**
	 * Tests {@link BigRational#factorial(int)}.
	 */
	@Test(expected = ArithmeticException.class)
	public void testFactorialNegative() {
		factorial(-1);
	}
	
	/**
	 * Tests {@link BigRational#sqrt(BigRational, int)}.
	 */
	@Test
	public void testSqrt() {
		for (int i = 0; i < 10; i++) {
			assertEquals(Math.sqrt(i), sqrt(valueOf(i), SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}

	/**
	 * Tests {@link BigRational#exp(BigRational, int)}.
	 */
	@Test
	public void testExp() {
		for (int i = 0; i < 10; i++) {
			assertEquals(Math.exp(i), exp(valueOf(i), SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}

	/**
	 * Tests {@link BigRational#log(BigRational, int)}.
	 */
	@Test
	public void testLog() {
		for (BigRational i = valueOf(0.1); i.compareTo(valueOf(2)) < 0; i=i.add(valueOf(0.1))) {
			System.out.println("LOG " + i);
			assertEquals(Math.log(i.toDouble()), log(i, SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}

	/**
	 * Tests {@link BigRational#log(BigRational, int)}.
	 */
	@Test(expected=ArithmeticException.class)
	public void testLogWith0() {
		log(ZERO, SCALE_FOR_DOUBLE_COMPARISON);
	}

	/**
	 * Tests {@link BigRational#sin(BigRational, int)}.
	 */
	@Test
	public void testSin() {
		for (int i = 0; i < 10; i++) {
			assertEquals(Math.sin(i), sin(valueOf(i), SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}

	/**
	 * Tests {@link BigRational#cos(BigRational, int)}.
	 */
	@Test
	public void testCos() {
		for (int i = 0; i < 10; i++) {
			assertEquals(Math.cos(i), cos(valueOf(i), SCALE_FOR_DOUBLE_COMPARISON).toDouble(), DOUBLE_ACCURACY);
		}
	}

	/**
	 * Tests {@link BigRational#pi(int)}.
	 */
	@Test
	public void testPi() {
		BigRational REFERENCE_PI = BigRational.valueOf(PI_STRING);
		for (int i = 0; i < PI_STRING.length() - 3; i++) {
			BigRational extectedPi = REFERENCE_PI.withScale(i);
			BigRational actualPi = pi(i);
			assertEquals(extectedPi, actualPi);
		}		
	}
	
	/**
	 * Tests the {@link Context}.
	 */
	@Test
	public void testContext() {
		BigDecimal BIGDECIMAL_PI = new BigDecimal(PI_STRING);
		double x = 1.5;
		double y = 2.1;
		
		for (int scale = 0; scale < 10; scale++) {
			Context context = new Context(scale);
			assertEquals(scale, context.getScale());
			
			assertEquals(BIGDECIMAL_PI.setScale(scale, RoundingMode.HALF_UP).toString(), context.pi().toPlainString());
			assertEquals("scale="+scale, toString(Math.sin(x), scale), context.sin(valueOf(x)).toPlainString());
			assertEquals("scale="+scale, toString(Math.cos(x), scale), context.cos(valueOf(x)).toPlainString());
			assertEquals("scale="+scale, toString(Math.exp(x), scale), context.exp(valueOf(x)).toPlainString());
			assertEquals("scale="+scale, toString(Math.sqrt(x), scale), context.sqrt(valueOf(x)).toPlainString());
			assertEquals("scale="+scale, toString(Math.log(x), scale), context.log(valueOf(x)).toPlainString());
			assertEquals("scale="+scale, toString(Math.pow(x, y), scale), context.pow(valueOf(x), valueOf(y)).toPlainString());
		}		
	}

	private static String toString(double value, int scale) {
		return trimTrailingFractionZeroes(new BigDecimal(String.valueOf(value)).setScale(scale, RoundingMode.HALF_UP).toPlainString());
	}

	private static String trimTrailingFractionZeroes(String string) {
		if (string.indexOf('.') == -1) {
			return string;
		}
		char[] charArray = string.toCharArray();
		int end = charArray.length - 1;
		while (end >= 0 && (charArray[end] == '0' ||  charArray[end] == '.')) {
			end--;
		}
		return string.substring(0, end+1);
	}
}
