package arnav.example.finmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nafis.bottomnavigation.NafisBottomNavigation;

import arnav.example.finmate.fragments.AddFragment;
import arnav.example.finmate.fragments.AnalysisFragment;
import arnav.example.finmate.fragments.BlogFragment;
import arnav.example.finmate.fragments.ChatFragment;
import arnav.example.finmate.fragments.HomeFragment;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class HomeActivity extends AppCompatActivity {
    private static final int idHome = 1;
    private static final int idAnalyses = 2;
    private static final int idAdd = 3;
    private static final int idBlog = 4;
    private static final int idChatBot = 5;
    NafisBottomNavigation navBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navBar = findViewById(R.id.navBar);

        navBar.add(new NafisBottomNavigation.Model(idHome, R.drawable.home_icon));
        navBar.add(new NafisBottomNavigation.Model(idAnalyses, R.drawable.analysis_icon));
        navBar.add(new NafisBottomNavigation.Model(idAdd, R.drawable.add_icon));
        navBar.add(new NafisBottomNavigation.Model(idBlog, R.drawable.blog_icon3));
        navBar.add(new NafisBottomNavigation.Model(idChatBot, R.drawable.chat_bot_icon));

        loadFragment(new HomeFragment(), 0);

        navBar.setOnClickMenuListener(new Function1<NafisBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(NafisBottomNavigation.Model model) {

                if (model.getId() == idHome) {
                    loadFragment(new HomeFragment(),1);
                } else if (model.getId() == idAnalyses) {
                    loadFragment(new AnalysisFragment(),1);
                } else if (model.getId() == idAdd) {
                    loadFragment(new AddFragment(),1);
                } else if (model.getId() == idBlog) {
                    loadFragment(new BlogFragment(),1);
                } else if (model.getId() == idChatBot) {
                    loadFragment(new ChatFragment(),1);
                }

                return null;
            }
        });

//        navBar.setOnShowListener(new Function1<NafisBottomNavigation.Model, Unit>() {
//            @Override
//            public Unit invoke(NafisBottomNavigation.Model model) {
//
//                if (model.getId() == idHome) {
//                    loadFragment(new HomeFragment(),1);
//                } else if (model.getId() == idAnalyses) {
//                    loadFragment(new AnalysisFragment(),1);
//                } else if (model.getId() == idAdd) {
//                    loadFragment(new AddFragment(),1);
//                } else if (model.getId() == idBlog) {
//                    loadFragment(new BlogFragment(),1);
//                } else if (model.getId() == idChatBot) {
//                    loadFragment(new ChatBotFragment(),1);
//                }
//                return null;
//            }
//        });
    }

    private void loadFragment(Fragment fragment, int flag) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (flag == 0) {
            ft.add(R.id.container, fragment);
        } else {
            ft.replace(R.id.container, fragment);
        }
        ft.commit();
    }
}