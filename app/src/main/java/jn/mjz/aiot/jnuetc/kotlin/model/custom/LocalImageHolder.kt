package jn.mjz.aiot.jnuetc.kotlin.model.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bigkoo.convenientbanner.holder.Holder
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.youth.xframe.XFrame
import jn.mjz.aiot.jnuetc.kotlin.R

/**
 * @author qq1962247851
 * @date 2020/1/18 19:49
 */
class LocalImageHolder(itemView: View, private val context: Context) :
    Holder<String>(itemView) {
    private lateinit var photoView: PhotoView
    private lateinit var progressBar: ProgressBar
    override fun initView(itemView: View) {
        photoView = itemView.findViewById(R.id.photoView)
        progressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun updateUI(data: String?) {
        Glide.with(context)
            .load(data)
            .error(R.drawable.xloading_error)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    photoView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    photoView.setBackgroundColor(XFrame.getColor(R.color.WindowBackgroundColor))
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    photoView.scaleType = ImageView.ScaleType.CENTER_CROP
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(photoView)
    }

    fun setPhotoViewEnable(enable: Boolean) {
        if (enable) {
            photoView.enable()
        } else {
            photoView.disenable()
        }
    }

}