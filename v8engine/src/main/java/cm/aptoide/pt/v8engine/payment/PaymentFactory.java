/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 11/08/2016.
 */

package cm.aptoide.pt.v8engine.payment;

import android.content.Context;
import android.support.annotation.NonNull;
import cm.aptoide.pt.model.v3.PaymentServiceResponse;
import cm.aptoide.pt.v8engine.BuildConfig;
import cm.aptoide.pt.v8engine.payment.providers.paypal.PayPalPayment;
import cm.aptoide.pt.v8engine.repository.RepositoryFactory;
import com.paypal.android.sdk.payments.PayPalConfiguration;

/**
 * Created by marcelobenites on 8/10/16.
 */
public class PaymentFactory {

  public static final String PAYPAL = "paypal";
  public static final String BOACOMPRA = "boacompra";
  public static final String BOACOMPRAGOLD = "boacompragold";
  public static final String DUMMY = "dummy";

  private final Context context;

  public PaymentFactory(Context context) {
    this.context = context;
  }

  public Payment create(PaymentServiceResponse paymentService, Product product) {
    switch (paymentService.getShortName()) {
      case PAYPAL:
        return new PayPalPayment(context, paymentService.getId(), paymentService.getShortName(),
            paymentService.getName(), paymentService.getSign(),
            getPrice(paymentService.getPrice(), paymentService.getCurrency(),
                paymentService.getTaxRate(), paymentService.getSign()), getPayPalConfiguration(),
            product, paymentService.getDescription(),
            RepositoryFactory.getPaymentConfirmationRepository(context, product),
            paymentService.isAuthorizationRequired());
      case BOACOMPRA:
      case BOACOMPRAGOLD:
      case DUMMY:
        return new AptoidePayment(paymentService.getId(), paymentService.getShortName(),
            paymentService.getName(), paymentService.getDescription(), product,
            getPrice(paymentService.getPrice(), paymentService.getCurrency(),
                paymentService.getTaxRate(), paymentService.getSign()),
            paymentService.isAuthorizationRequired(),
            RepositoryFactory.getPaymentConfirmationRepository(context, product));
      default:
        throw new IllegalArgumentException(
            "Payment not supported: " + paymentService.getShortName());
    }
  }

  @NonNull
  private Price getPrice(double price, String currency, double taxRate, String currencySymbol) {
    return new Price(price, currency, currencySymbol, taxRate);
  }

  private PayPalConfiguration getPayPalConfiguration() {
    final PayPalConfiguration configuration = new PayPalConfiguration();
    configuration.environment(BuildConfig.PAYPAL_ENVIRONMENT);
    configuration.clientId(BuildConfig.PAYPAL_KEY);
    return configuration;
  }
}