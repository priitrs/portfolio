package ee.bank.portfolio.config;

import java.math.MathContext;
import java.math.RoundingMode;

public final class MathContexts {
    private MathContexts() {}
    public static final MathContext FINANCE = new MathContext(6, RoundingMode.HALF_UP);
    public static final MathContext PERCENT = new MathContext(2, RoundingMode.HALF_UP);
}
