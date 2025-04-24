package arnav.example.finmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView navBar;
    MaterialToolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView nav_drawer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    TextView toolbarTitle;
    NavController navController;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hideSystemUI();
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbar = findViewById(R.id.toolbar);
        navBar = findViewById(R.id.navBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        nav_drawer = findViewById(R.id.nav_drawer);


        /*Initializing database objects*/
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        /*Tool Bar*/
        setSupportActionBar(toolbar);

        /*Creating Map*/
        Map<Integer, Integer> menuToFragmentMap = new HashMap<>();
        menuToFragmentMap.put(R.id.home, R.id.homeFragment);
        menuToFragmentMap.put(R.id.analyze, R.id.analyzeFragment);
        menuToFragmentMap.put(R.id.blog, R.id.blogFragment);
        menuToFragmentMap.put(R.id.chat, R.id.chatFragment);
        menuToFragmentMap.put(R.id.setBudget, R.id.budgetFragment);
        menuToFragmentMap.put(R.id.goal, R.id.savingGoalFragment);

        /*Opening and Closing Navigation Drawer and setting actions to toolbar menu's items*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer);
        toggle.setDrawerIndicatorEnabled(false);//this will disable implicitly given menu button on toolbar

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_menu) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
                return true;
            }
            if (item.getItemId() == R.id.action_notification) {
                return true;
            }
            return false;
        });
        nav_drawer.bringToFront();

        /*Setting actions to Drawer menu's items*/
        nav_drawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuId = item.getItemId();
                Integer destinationId = menuToFragmentMap.get(menuId);
                if (destinationId != null) {
                    navController.navigate(destinationId);
                } else {
                    if (menuId == R.id.logout) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            }
        });




        /*Bottom Navigation Bar*/
        navBar.getMenu().getItem(2).setEnabled(false);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
//            NavigationUI.setupWithNavController(navBar, navController);
        } else {
            throw new IllegalStateException("NavHostFragment not found");
        }


        navBar.setOnItemSelectedListener(item -> {
            int menuId = item.getItemId();
            Integer destinationId = menuToFragmentMap.get(menuId);

            if (destinationId != null && navController.getCurrentDestination().getId() != destinationId) {
                navController.navigate(destinationId);
            }

            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            for (Map.Entry<Integer, Integer> entry : menuToFragmentMap.entrySet()) {
                if (entry.getValue() == destination.getId()) {
                    navBar.setSelectedItemId(entry.getKey());
                    break;
                }
            }

            /*Dynamically changing toolbar title based on fragment change*/
            if (destination.getId() == R.id.homeFragment) {
                db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String[] name = documentSnapshot.getString("name").split(" ");
                                // Use the name wherever needed
                                toolbarTitle.setText("Hello " + name[0]);
                                Log.d("Username", "User name: " + name[0]);
                            } else {
                                Log.d("Username", "No such document");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Username", "Failed to fetch user name", e);
                        });
            } else if (destination.getId() == R.id.analyzeFragment) {
                toolbarTitle.setText("Analytics");
            } else if (destination.getId() == R.id.addFragment) {
                toolbarTitle.setText("Add Transaction");
            }else if (destination.getId() == R.id.blogFragment) {
                toolbarTitle.setText("Blogs");
            }else if (destination.getId() == R.id.chatFragment) {
                toolbarTitle.setText("Chat Bot");
            }else if (destination.getId() == R.id.budgetFragment) {
                toolbarTitle.setText("Budget");
            }else if (destination.getId() == R.id.savingGoalFragment) {
                toolbarTitle.setText("Saving Goals");
            }
        });

        navBar.setSelectedItemId(R.id.home);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (navController.getCurrentDestination().getId() != R.id.addFragment) {
                navController.navigate(R.id.addFragment);
            }
        });



        //Back Press Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else if (!navController.popBackStack()) {
                    finish();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Handle back press with NavController
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

}