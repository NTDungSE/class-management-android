//package vn.edu.fpt.studentmanagementapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//
//import vn.edu.fpt.studentmanagementapp.view.activities.HomeActivity;
//import vn.edu.fpt.studentmanagementapp.view.activities.auth.LoginActivity;
//
//public class MainActivity extends AppCompatActivity {
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mAuth = FirebaseAuth.getInstance();
//
//        // Check if user is already logged in
//        if (mAuth.getCurrentUser() != null) {
//            // Just redirect to the unified dashboard
//            startActivity(new Intent(this, HomeActivity.class));
//            finish();
//        } else {
//            // Redirect to LoginActivity
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//        }
//    }
//}