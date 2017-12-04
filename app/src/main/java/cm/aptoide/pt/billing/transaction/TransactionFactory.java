package cm.aptoide.pt.billing.transaction;

public class TransactionFactory {

  public SimpleTransaction create(String id, String customerId, String productId,
      Transaction.Status status, String serviceId) {
    return new SimpleTransaction(id, status, customerId, productId, serviceId);
  }
}