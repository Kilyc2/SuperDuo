package barqsoft.footballscores.Widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.Utils.CursorUtil;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;

    private ArrayList<String> matches = new ArrayList<>();
    private Context context;
    private ScoresProvider mContent;

    public WidgetFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        this.matches.clear();
        long identityToken = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            Cursor cursor =
                    contentResolver.query(DatabaseContract.scores_table.buildScoreWithDate(),
                            null, null, getToday(), null);
            if (CursorUtil.isValidCursor(cursor)) {
                do {
                    String match = cursor.getString(COL_HOME) + ";" +
                            cursor.getString(COL_AWAY) + ";" +
                            cursor.getString(COL_MATCHTIME) + ";" +
                            Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)) + ";" +
                            cursor.getDouble(COL_ID) + ";" +
                            Utilies.getTeamCrestByTeamName(cursor.getString(COL_HOME)) + ";" +
                            Utilies.getTeamCrestByTeamName(cursor.getString(COL_AWAY));
                    this.matches.add(match);
                } while (cursor.moveToNext());
            } else {
                this.matches.add("");
            }
            CursorUtil.closeCursor(cursor);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private String[] getToday() {
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String[] today = {mformat.format(fragmentdate)};
        return today;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        String match = this.matches.get(position);
        if (match.isEmpty()) {
            RemoteViews mView = new RemoteViews(context.getPackageName(),
                    R.layout.no_matches_item);
            return mView;
        } else {
            RemoteViews mView = new RemoteViews(context.getPackageName(),
                    R.layout.scores_list_item);
            String[] matchValues = match.split(";");
            mView.setTextViewText(R.id.home_name, matchValues[0]);
            mView.setTextViewText(R.id.away_name, matchValues[1]);
            mView.setTextViewText(R.id.data_textview, matchValues[2]);
            mView.setTextViewText(R.id.score_textview, matchValues[3]);
            return mView;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
