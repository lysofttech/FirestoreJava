package yourpackagename.fb;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

// Step 1 - This interface defines the type of messages I want to communicate to my owner
public interface FBDataListener {
    // These methods are the different events and
    // need to pass relevant arguments related to the event triggered
    void onDataAvailable(Task<QuerySnapshot> task);

    // or when data has been loaded
    void onNoData(String data);
}
