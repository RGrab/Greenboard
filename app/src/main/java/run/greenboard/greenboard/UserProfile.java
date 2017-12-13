package run.greenboard.greenboard;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by Rocky on 12/1/16.
 */
public class UserProfile extends Fragment {
    View view;
    String username;
    TextView usernameTextView;

    public UserProfile() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_user_profile, container, false);
        SharedPreferences user = getActivity().getSharedPreferences("UserData", 0);
        username = user.getString("username", "");
        usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);
        return view;
    }
}
