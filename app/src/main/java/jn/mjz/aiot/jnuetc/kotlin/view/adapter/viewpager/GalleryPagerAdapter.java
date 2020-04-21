package jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import jn.mjz.aiot.jnuetc.kotlin.R;


/**
 * @author 19622
 */
public class GalleryPagerAdapter extends PagerAdapter {

    private boolean enable;
    private List<String> urls;
    private Context context;
    private IGalleryListener i;

    public GalleryPagerAdapter(boolean enable, List<String> urls, Context context, IGalleryListener i) {
        this.enable = enable;
        this.urls = urls;
        this.context = context;
        this.i = i;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View viewWithTag = container.findViewWithTag(position);
        if (viewWithTag == null) {
            viewWithTag = View.inflate(context, R.layout.view_pager_item_gallery, null);
            viewWithTag.setTag(position);
            container.addView(viewWithTag);
            PhotoView photoView = viewWithTag.findViewById(R.id.photoView);
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ProgressBar progressBar = viewWithTag.findViewById(R.id.progressBar);
            Glide.with(context)
                    .load(urls.get(position))
                    .error(R.drawable.xloading_error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            photoView.setOnClickListener(v -> i.onPhotoClick());
                            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    i.onPhotoLongClick(position);
                                    return true;
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            photoView.setBackgroundColor(ContextCompat.getColor(context, R.color.WindowBackgroundColor));
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(photoView);
            if (enable) {
                photoView.enable();
            } else {
                photoView.disenable();
            }
        }
        return viewWithTag;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public interface IGalleryListener {
        /**
         * 点击图片
         */
        void onPhotoClick();

        void onPhotoLongClick(@NotNull Integer position);
    }
}
