package cm.aptoide.pt.billing.payment;

import rx.Single;

public class AdyenPaymentService extends PaymentService {

  private final Adyen adyen;

  public AdyenPaymentService(String id, String type, String name, String description, String icon,
      Adyen adyen, boolean defaultService) {
    super(id, type, name, description, icon, defaultService);
    this.adyen = adyen;
  }

  public Single<String> getToken() {
    return adyen.createToken();
  }
}
