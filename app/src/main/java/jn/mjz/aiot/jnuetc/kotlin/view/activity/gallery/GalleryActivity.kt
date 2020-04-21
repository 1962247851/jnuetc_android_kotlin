package jn.mjz.aiot.jnuetc.kotlin.view.activity.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.youth.xframe.utils.http.XHttp
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.util.FileUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager.GalleryPagerAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import kotlinx.android.synthetic.main.activity_gallery.*

/**
 * @author 19622
 */
class GalleryActivity : AbstractActivity(), GalleryPagerAdapter.IGalleryListener {
    private var firstIndex = 0
    private var currentItem = 0
    private lateinit var urls: ArrayList<String>
    private var galleryPagerAdapter: GalleryPagerAdapter? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(URLS, urls)
        outState.putInt(FIRST_INDEX, firstIndex)
        outState.putInt(CURRENT_ITEM, currentItem)
    }

    override fun preFinish(): Boolean {
        val intent = Intent()
        intent.putExtra(CURRENT_ITEM, currentItem)
        setResult(Activity.RESULT_OK, intent)
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        // 延迟共享动画的执行
        postponeEnterTransition()
        return R.layout.activity_gallery
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (intent != null) {
            urls = GsonUtil.parseJsonArray2List(
                intent.getStringExtra(URLS),
                String::class.java
            )
            firstIndex = intent.getIntExtra(FIRST_INDEX, 0)
            currentItem = firstIndex
        } else if (savedInstanceState != null) {
            urls = savedInstanceState.getStringArrayList(URLS) as ArrayList<String>
            firstIndex = savedInstanceState.getInt(FIRST_INDEX)
            currentItem = savedInstanceState.getInt(CURRENT_ITEM)
        }
        galleryPagerAdapter =
            GalleryPagerAdapter(
                true,
                urls,
                this,
                this
            )
    }

    override fun initView() {
        viewPager_gallery.adapter = galleryPagerAdapter
        viewPager_gallery.currentItem = firstIndex
        updateBottomBar()
        XHttp.handler.postDelayed(
            { supportStartPostponedEnterTransition() },
            100
        )
        viewPager_gallery.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) { //ignore
            }

            override fun onPageSelected(position: Int) {
                currentItem = position
                updateBottomBar()
            }

            override fun onPageScrollStateChanged(state: Int) { //ignore
            }
        })
        image_view_download_photo.setOnClickListener {
            FileUtil.saveFileByGlide(
                this,
                urls[currentItem],
                urls[currentItem].substringAfter("fileName=")
            )
        }
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.statusBarColor = Color.TRANSPARENT
    }

    @SuppressLint("SetTextI18n")
    private fun updateBottomBar() {
        text_view_indicator.text = "${currentItem + 1}/${urls.size}"
    }

    companion object {
        const val URLS = "urls"
        const val FIRST_INDEX = "firstIndex"
        const val CURRENT_ITEM = "currentItem"
    }

    override fun onPhotoClick() {
        val intent = Intent()
        intent.putExtra(CURRENT_ITEM, currentItem)
        setResult(Activity.RESULT_OK, intent)
        finishAfterTransition()
    }

    override fun onPhotoLongClick(position: Int) {
        AlertDialog.Builder(this)
            .setPositiveButton(
                "保存到本地"
            ) { _, _ ->
                FileUtil.saveFileByGlide(
                    this,
                    urls[position],
                    urls[position].substringAfter("fileName=")
                )
            }.show()
    }

}