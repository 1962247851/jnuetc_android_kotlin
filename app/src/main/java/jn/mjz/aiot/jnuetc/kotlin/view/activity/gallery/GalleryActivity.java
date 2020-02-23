package jn.mjz.aiot.jnuetc.kotlin.view.activity.gallery;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.youth.xframe.utils.statusbar.XStatusBar;

import java.util.ArrayList;
import java.util.List;

import jn.mjz.aiot.jnuetc.kotlin.R;
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil;
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager.GalleryPagerAdapter;

/**
 * @author 19622
 */
public class GalleryActivity extends AppCompatActivity {

    public static final String URLS = "urls";
    public static final String FIRST_INDEX = "firstIndex";
    private int firstIndex = 0;
    private List<String> urls;
    private GalleryPagerAdapter galleryVPAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_gallery);
        viewPager = findViewById(R.id.viewPager_gallery);
        XStatusBar.setTransparent(this);
        if (getIntent() != null) {
            urls = GsonUtil.parseJsonArray2List(getIntent().getStringExtra(URLS), String.class);
            firstIndex = getIntent().getIntExtra(FIRST_INDEX, 0);
        } else if (savedInstanceState != null) {
            urls = savedInstanceState.getStringArrayList(URLS);
            firstIndex = savedInstanceState.getInt(FIRST_INDEX);
        }
        galleryVPAdapter = new GalleryPagerAdapter(true, urls, this, this::finish);
        viewPager.setAdapter(galleryVPAdapter);
        viewPager.setCurrentItem(firstIndex);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(URLS, new ArrayList<>(urls));
        outState.putInt(FIRST_INDEX, firstIndex);
    }
}
