package com.dcb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class MoneyCalc {

  /**
   * Applies fee and tax to a base amount with correct currency rounding.
   *
   * @param amountStr  monetary amount as String, e.g., "1234.56"
   * @param feeRateStr fee rate as decimal String, e.g., "0.015" for 1.5%
   * @param taxRateStr tax rate as decimal String, e.g., "0.18" for 18%
   * @param currency   Java Currency to derive fraction digits (e.g., Currency.getInstance("INR"))
   * @return total amount after fee and tax, rounded to currency minor units
   */
  public static BigDecimal applyFeeAndTax(String amountStr, String feeRateStr, String taxRateStr,
      Currency currency) {
    // TODO: Implement the calculation logic here
    // Hints:
    // 1. Get the correct number of decimal places from the Currency object

    int defaultFractionDigits = currency.getDefaultFractionDigits();
    // 2. Use RoundingMode.HALF_UP for rounding operations
    BigDecimal amount = new BigDecimal(amountStr);
    BigDecimal feeRate = new BigDecimal(feeRateStr);
    BigDecimal taxRate = new BigDecimal(taxRateStr);
    // 3. Calculate fee on the base amount and round
    BigDecimal amountFee = amount.multiply(feeRate)
        .setScale(defaultFractionDigits, RoundingMode.HALF_UP);

    // 4. Calculate tax on the (base amount + fee) and round
    BigDecimal taxBaseAmt = amount.add(amountFee)
        .setScale(defaultFractionDigits, RoundingMode.HALF_UP);
    BigDecimal tax = taxBaseAmt.multiply(taxRate)
        .setScale(defaultFractionDigits, RoundingMode.HALF_UP);
    // 5. Return the final total

    return amount.add(amountFee).add(tax).setScale(defaultFractionDigits, RoundingMode.HALF_UP);
  }

  /**
   * Overload using long cents/paise (recommended for avoiding FP in outer layers).
   *
   * @param amountCents monetary amount in minor units, e.g., 123456 for 1234.56
   * @param feeRateStr  fee rate as decimal String
   * @param taxRateStr  tax rate as decimal String
   * @param currency    Java Currency context
   * @return total amount after fee and tax in minor units (cents/paise)
   */
  public static long applyFeeAndTaxCents(long amountCents, String feeRateStr, String taxRateStr,
      Currency currency) {
    // TODO: Implement using minor integer units as the starting point.
    // You will still need BigDecimal internally to calculate the percentages safely.
    int defaultRoundUp = currency.getDefaultFractionDigits();
    BigDecimal amountCent = new BigDecimal(String.valueOf(amountCents));
    BigDecimal feeRate = new BigDecimal(feeRateStr);
    BigDecimal taxRate = new BigDecimal(taxRateStr);

    BigDecimal fee = amountCent.multiply(feeRate);
    BigDecimal taxBase = amountCent.add(fee);
    BigDecimal tax = taxBase.multiply(taxRate);
    BigDecimal total = taxBase.add(tax);
    return total.longValue();
  }

  public static void main(String[] args) {
    Currency INR = Currency.getInstance("INR");

    System.out.println("--- Testing applyFeeAndTax ---");
    BigDecimal total = applyFeeAndTax("1234.56", "0.015", "0.18", INR);
    System.out.println("Calculated Total (INR): " + total);
    System.out.println("Expected Total (INR):   1478.63");

    System.out.println("\n--- Testing applyFeeAndTaxCents ---");
    long totalCents = applyFeeAndTaxCents(123456, "0.015", "0.18", INR);
    System.out.println("Calculated Total in paise: " + totalCents);
    System.out.println("Expected Total in paise:   147863");
  }
}