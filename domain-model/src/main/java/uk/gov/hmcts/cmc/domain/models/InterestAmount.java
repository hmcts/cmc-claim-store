package uk.gov.hmcts.cmc.domain.models;

public class InterestAmount {

    public static final int FACTOR = 100;

    private double amount;

    public InterestAmount(double amount) {
        this.setAmount(amount);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = (double) Math.round(amount * FACTOR) / FACTOR;
    }

    public static InterestAmount valueOf(final double amount) {
        return new InterestAmount(amount);
    }
}
