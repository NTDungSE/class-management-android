package vn.edu.fpt.studentmanagementapp.view.activities.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import vn.edu.fpt.studentmanagementapp.R;
import vn.edu.fpt.studentmanagementapp.view.activities.HomeActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "GoogleSignIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnGoToRegister = findViewById(R.id.btn_go_to_register);
        SignInButton btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            checkUserRoleAndRedirect();
                        } else {
                            String errorMessage = "Login failed";
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    errorMessage = "No account found with this email";
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage = "Invalid password";
                                } else {
                                    errorMessage = task.getException().getMessage();
                                }
                            }
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Google Sign-In button
        btnGoogleSignIn.setOnClickListener(v -> signIn());

        // Chuyển sang màn hình đăng ký
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
            });

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Successfully signed in with Google, now authenticate with Firebase
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        // Check if new user
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            // Create user in Firestore
                            createUserInFirestore();
                        } else {
                            // Existing user, check role and redirect
                            checkUserRoleAndRedirect();
                        }
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ?
                                                task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserInFirestore() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // Default to student role for Google sign-in
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", "student"); // Default role

        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "User profile created", Toast.LENGTH_SHORT).show();
                    // Redirect to role selection or student dashboard
                    showRoleSelectionDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRoleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select your role");
        String[] roles = {"Student", "Teacher"};

        builder.setItems(roles, (dialog, which) -> {
            String role = which == 0 ? "student" : "teacher";
            updateUserRole(role);
        });

        builder.setCancelable(false); // Force user to select a role
        builder.create().show();
    }

    private void updateUserRole(String role) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update("role", role)
                .addOnSuccessListener(aVoid -> {
                    if ("teacher".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        // If student role, prompt for additional info
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRoleAndRedirect() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("teacher".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        // Create user document if it doesn't exist (for users who registered before)
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", mAuth.getCurrentUser().getEmail());
                        user.put("role", "student"); // Default role

                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(LoginActivity.this, "Created new user profile", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);

        builder.setView(view)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    if (!email.isEmpty()) {
                        sendPasswordResetEmail(email);
                    } else {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    String message = task.isSuccessful() ?
                            "Reset link sent to your email" :
                            "Error: " + Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }
}