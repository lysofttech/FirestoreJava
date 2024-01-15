/* Class code */
package yourpackagename.fb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FBData {
    private String TableName;
    private boolean IsLimited = false;
    private int iLimit = 10;
    public boolean IsRefreshRequired = true;
    Context context;
    List<String> whereCols = new ArrayList<>();
    List<String> whereOps = new ArrayList<>();
    List<String> whereVals = new ArrayList<>();
    List<String> orderBy = new ArrayList<>();
    List<Query.Direction> directions = new ArrayList<>();
    private Dialog progress;
    AnimatorSet mAnimationSet;

    public boolean isLimited() {
        return IsLimited;
    }

    public void setLimited(boolean limited) {
        IsLimited = limited;
    }

    public void setLimit(int limit) {
        iLimit = limit;
    }

    private FBDataListener listener;

    public FBData(Context c, String tableName) {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
        this.context = c;
        this.TableName = tableName;
        if (this.context != null) {
            ProgressShow();
        }
    }

    // Assign the listener implementing events interface that will receive the events
    public void setFBClassListener(FBDataListener listener) {
        this.listener = listener;
    }

    public void addOrderBy(String OrderBy, Query.Direction direction) {
        this.orderBy.add(OrderBy);
        this.directions.add(direction);
    }

    public void addOrderBy(String OrderBy, boolean Descending) {
        Query.Direction direction = Query.Direction.ASCENDING;
        if (Descending)
            direction = Query.Direction.DESCENDING;
        this.orderBy.add(OrderBy);
        this.directions.add(direction);
    }

    public void addWhere(String whereCol, String ops, String whereValue) {
        this.whereCols.add(whereCol);
        this.whereOps.add(ops);
        this.whereVals.add(whereValue);
    }

    public void addWhere(String whereCol, String whereValue) {
        this.whereCols.add(whereCol);
        this.whereOps.add("==");
        this.whereVals.add(whereValue);
    }

    public void FetchData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.enableNetwork();
//        if (!IsRefreshRequired) {
//            db.disableNetwork();
//        }

        Query cr = db.collection(this.TableName);
        if (this.whereVals.size() > 0) {
            for (int i = 0; i < whereVals.size(); i++) {
                //mdl.Log("Where " + whereCols.get(i) + " " + whereOps.get(i) + " " + whereVals.get(i));
                if (whereOps.get(i).equalsIgnoreCase("==")) {
                    cr = cr.whereEqualTo(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase("=")) {
                    cr = cr.whereEqualTo(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase(">")) {
                    cr = cr.whereGreaterThan(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase(">=")) {
                    cr = cr.whereGreaterThanOrEqualTo(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase("<")) {
                    cr = cr.whereLessThan(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase("<=")) {
                    cr = cr.whereLessThanOrEqualTo(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase("<>")) {
                    cr = cr.whereNotEqualTo(whereCols.get(i), whereVals.get(i));
                } else if (whereOps.get(i).equalsIgnoreCase("!=")) {
                    cr = cr.whereNotEqualTo(whereCols.get(i), whereVals.get(i));
                }
//                else if (whereOps.get(i).equalsIgnoreCase("<>")) {
//                    cr.where(whereCols.get(i), whereVals.get(i));
//                }
            }
        }
        if (this.orderBy.size() > 0) {
            for (int i = 0; i < orderBy.size(); i++) {
                cr = cr.orderBy(this.orderBy.get(i), this.directions.get(i));
            }
        }

        if (this.IsLimited) {
            mdl.Log("Limiting to only " + iLimit);
            cr = cr.limit(iLimit);
        }

        cr.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                FirebaseTaskCompleted(task);
            }
        });
    }

    private void FirebaseTaskCompleted(Task<QuerySnapshot> task) {
        if (context != null) {
            ProgressClose();
        }

        if (task.isSuccessful()) {
            this.listener.onDataAvailable(task);
        } else {
            this.listener.onNoData("No data available");
        }
    }


    private void ProgressShow() {
        if (context != null) {
            progress = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            Objects.requireNonNull(progress.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progress.setContentView(R.layout.loading);
            progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                    }
                    return true;
                }
            });
            progress.setCanceledOnTouchOutside(false);
            TextView reload = progress.findViewById(R.id.loadingimg);
            TextView Loading_txt = progress.findViewById(R.id.Loading_txt);

            if (!GlobalSettings.IsArabic()) {
                Loading_txt.setText("Please Wait");
            }

            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(reload, "alpha", 1f, .1f);
            fadeOut.setDuration(1000);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(reload, "alpha", .1f, 1f);
            fadeIn.setDuration(1000);

            mAnimationSet = new AnimatorSet();

            mAnimationSet.play(fadeIn).after(fadeOut);

            mAnimationSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimationSet.start();
                }
            });
            mAnimationSet.start();

            if (context != null) {
                if (progress != null)
                    progress.show();
            }
        }
    }

    private void ProgressClose() {
        if (context != null) {
            try {
                //if (!((Activity) context).isFinishing())
                if (progress != null && progress.isShowing())
                    progress.dismiss();
            } catch (Exception ignored) {
            }
            mAnimationSet.cancel();
        }
    }
}
