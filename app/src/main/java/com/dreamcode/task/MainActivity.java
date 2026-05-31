package com.dreamcode.task;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dreamcode.task.databinding.ActivityMainBinding;
import com.dreamcode.task.databinding.DialogPinEntryBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    private static final String PREFS_NAME = "DreamCodeNotesPrefs";
    private static final String KEY_VAULT_PIN = "vault_pin";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications permission denied. Reminders won't show.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to SecondFragment from allowed screens
                if (navController.getCurrentDestination() != null) {
                    int currentId = navController.getCurrentDestination().getId();
                    if (currentId == R.id.FirstFragment || currentId == R.id.CalendarFragment) {
                        navController.navigate(R.id.SecondFragment);
                    }
                }
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.SecondFragment || destination.getId() == R.id.SecretNotesFragment || destination.getId() == R.id.SettingsFragment) {
                binding.fab.setVisibility(View.GONE);
            } else {
                binding.fab.setVisibility(View.VISIBLE);
            }
        });

        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_settings) {
            navController.navigate(R.id.SettingsFragment);
            return true;
        } else if (id == R.id.action_calendar) {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.CalendarFragment) {
                navController.navigate(R.id.CalendarFragment);
            }
            return true;
        } else if (id == R.id.action_secret_notes) {
            handleSecretNotesAccess();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSecretNotesAccess() {
        if (BiometricHelper.isBiometricAvailable(this)) {
            BiometricHelper.showBiometricPrompt(this, new BiometricHelper.BiometricCallback() {
                @Override
                public void onAuthenticationSucceeded() {
                    navController.navigate(R.id.SecretNotesFragment);
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    checkPinFallback();
                }

                @Override
                public void onAuthenticationFailed() {
                }
            });
        } else {
            checkPinFallback();
        }
    }

    private void checkPinFallback() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPin = prefs.getString(KEY_VAULT_PIN, null);

        if (savedPin != null) {
            showPinEntryDialog(savedPin);
        } else {
            Toast.makeText(this, "Please set a Vault PIN in Settings to use the secret section.", Toast.LENGTH_LONG).show();
        }
    }

    private void showPinEntryDialog(String correctPin) {
        DialogPinEntryBinding pinBinding = DialogPinEntryBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Unlock Vault")
                .setView(pinBinding.getRoot())
                .setPositiveButton("Unlock", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String enteredPin = pinBinding.editTextPinEntry.getText().toString();
                if (enteredPin.equals(correctPin)) {
                    dialog.dismiss();
                    navController.navigate(R.id.SecretNotesFragment);
                } else {
                    Toast.makeText(MainActivity.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void showAboutDialog() {
        String versionName = "1.0.0";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String aboutMessage = getString(R.string.about_description) + "\n\n" +
                getString(R.string.about_features) + "\n\n" +
                getString(R.string.about_version, versionName) + "\n" +
                getString(R.string.about_credits);

        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(aboutMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
