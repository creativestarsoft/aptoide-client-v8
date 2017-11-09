package cm.aptoide.pt.search;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class SearchCursorAdapter extends CursorAdapter {

  private static final String[] COLUMN_NAMES = {
      "_id", android.app.SearchManager.SUGGEST_COLUMN_TEXT_1
  };

  public SearchCursorAdapter(Context context) {
    super(context, null, false);
  }

  public void setData(@NonNull List<String> data) {
    swapCursor(getCursorFor(data));
  }

  @NonNull private MatrixCursor getCursorFor(@NonNull List<String> data) {
    final MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES, data.size());
    int index = 0;
    for (String item : data) {
      cursor.newRow()
          .add(Integer.toString(index++))
          .add(item);
    }
    return cursor;
  }

  @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return LayoutInflater.from(context)
        .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
  }

  @Override public void bindView(View view, Context context, Cursor cursor) {
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    textView.setText(
        cursor.getString(cursor.getColumnIndex(android.app.SearchManager.SUGGEST_COLUMN_TEXT_1)));
  }
}