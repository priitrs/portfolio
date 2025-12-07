package ee.bank.portfolio;

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }
}
