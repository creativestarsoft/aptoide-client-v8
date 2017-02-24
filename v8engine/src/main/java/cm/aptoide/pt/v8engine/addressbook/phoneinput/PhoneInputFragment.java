package cm.aptoide.pt.v8engine.addressbook.phoneinput;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.pt.preferences.Application;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.addressbook.data.ContactsRepositoryImpl;
import cm.aptoide.pt.v8engine.fragment.SupportV4BaseFragment;
import com.jakewharton.rxbinding.view.RxView;

/**
 * Created by jdandrade on 14/02/2017.
 */
public class PhoneInputFragment extends SupportV4BaseFragment implements PhoneInputContract.View {

  private PhoneInputContract.UserActionsListener mActionsListener;
  private TextView mNotNowV;
  private TextView mSharePhoneV;
  private Button mSaveNumber;
  private EditText mCountryNumber;
  private EditText mPhoneNumber;

  public static PhoneInputFragment newInstance() {
    PhoneInputFragment phoneInputFragment = new PhoneInputFragment();
    Bundle extras = new Bundle();
    phoneInputFragment.setArguments(extras);
    return phoneInputFragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mActionsListener = new PhoneInputPresenter(this, new ContactsRepositoryImpl());
  }

  @Override public void setupViews() {
    mNotNowV.setPaintFlags(mNotNowV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    mSharePhoneV.setText(getString(R.string.addressbook_share_phone,
        Application.getConfiguration().getMarketName()));
    RxView.clicks(mNotNowV).subscribe(click -> this.mActionsListener.notNowClicked());
    RxView.clicks(mSaveNumber)
        .subscribe(click -> this.mActionsListener.submitClicked(
            mCountryNumber.getText().toString().concat(mPhoneNumber.getText().toString())));
  }

  @Override public int getContentViewId() {
    return R.layout.fragment_phone_input;
  }

  @Override public void bindViews(@Nullable View view) {
    mNotNowV = (TextView) view.findViewById(R.id.addressbook_not_now);
    mSharePhoneV = (TextView) view.findViewById(R.id.addressbook_share_phone_message);
    mSaveNumber = (Button) view.findViewById(R.id.addressbook_phone_input_save);
    mCountryNumber = (EditText) view.findViewById(R.id.addressbook_country_number);
    mPhoneNumber = (EditText) view.findViewById(R.id.addressbook_phone_number);
  }

  @Override public void finishView() {
    getActivity().onBackPressed();
  }

  @Override public void showProgressIndicator(boolean active) {
    // TODO: 14/02/2017 manipulate progress
  }

  @Override public void showSubmissionSuccess() {
    Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
  }

  @Override public void showSubmissionError() {
    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
  }
}
