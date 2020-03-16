package io.github.kaisendonone.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import io.github.kaisendonone.API.AccessToken
import io.github.kaisendonone.API.Status
import io.github.kaisendonone.Fragment.ProfileFragment
import io.github.kaisendonone.Fragment.TimeLineFragment
import io.github.kaisendonone.JSON.TimelineJSON
import io.github.kaisendonone.R
import io.github.kaisendonone.Util.checkMobileData
import kotlinx.android.synthetic.main.fragment_timeline.*

class TimelineAdapter(private val jsonStringArray: ArrayList<TimelineJSON>) :
    RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    lateinit var token: AccessToken
    lateinit var timeLineFragment: TimeLineFragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_timeline, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return jsonStringArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val json = jsonStringArray[position]

        holder.apply {
            val context = holder.avatarImageView.context
            val isMobileData = checkMobileData(context)

            contentTextView.text =
                HtmlCompat.fromHtml(json.content ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT)

            if (timeLineFragment.isUseBackground) {
                cardView.alpha = 0.8f
            }

            if (json.isNotification()) {
                // 通知と分岐
                userNameTextView.text =
                    "${json.getNotification()?.type} ${json.getNotification()?.displayName} @${json.getNotification()?.userName}"
                // モバイルデータ接続なら利用しない
                if (!isMobileData) {
                    Glide.with(avatarImageView)
                        .load(json.getNotification()?.avatarStatic)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30))) //←この一行追加
                        .into(holder.avatarImageView)
                    // ユーザー情報
                    avatarImageView.setOnClickListener {
                        timeLineFragment.apply {
                            changeFragment(ProfileFragment().apply {
                                arguments =
                                    Bundle().apply { putString("id", json.getNotification()?.userId) }
                            })
                        }
                    }
                } else {
                    avatarImageView.visibility = View.GONE
                }
                createAtTextView.text = json.getNotification()?.formattedTime
                // Fav消す
                (favTextView.parent as LinearLayout).visibility = View.GONE
            } else {
                // タイムライン
                userNameTextView.text = "${json.displayName} @${json.acct}"
                // モバイルデータ接続なら利用しない
                if (!isMobileData) {
                    Glide.with(avatarImageView)
                        .load(json.avatarStatic)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30))) //←この一行追加
                        .into(holder.avatarImageView)
                    // ユーザー情報
                    avatarImageView.setOnClickListener {
                        timeLineFragment.apply {
                            changeFragment(ProfileFragment().apply {
                                arguments = Bundle().apply { putString("id", json.userId) }
                            })
                        }
                    }
                } else {
                    avatarImageView.visibility = View.GONE
                }
                createAtTextView.text = json.formattedTime
                // Fav / BT
                val status = Status(token)
                favTextView.setOnClickListener {
                    Snackbar.make(timeLineFragment.fragment_timeline_fab_menu, "ふぁぼりますか？", Snackbar.LENGTH_SHORT)
                        .setAnchorView(timeLineFragment.fragment_timeline_fab_menu)
                        .setAction("ふぁぼ") {
                            status.statusFav(json.id) {
                                showToast(context, "ふぁぼりました：${json.id}")
                            }
                        }.show()
                }
                boostTextView.setOnClickListener {
                    Snackbar.make(timeLineFragment.fragment_timeline_fab_menu, "ブーストしますか？", Snackbar.LENGTH_SHORT)
                        .setAnchorView(timeLineFragment.fragment_timeline_fab_menu)
                        .setAction("ブースト") {
                            status.statusBoost(json.id) {
                                showToast(context, "ブーストしました：${json.id}")
                            }
                        }.show()
                }
            }

            //　添付画像
            // モバイルデータ接続なら利用しない
            if (isMobileData) {
                if (json.mediaUrlList.isNotEmpty()) {
                    val mediaImageView =
                        arrayListOf(holder.media1, holder.media2, holder.media3, holder.media4)
                    for (i in 0 until json.mediaUrlList.size) {
                        val url = json.mediaUrlList[i]
                        val imageView = mediaImageView[i]
                        Glide.with(imageView)
                            .load(url)
                            .into(imageView)
                        imageView.setOnClickListener {
                            startBrowser(it.context, url)
                        }
                    }
                    mediaLinearlayout.visibility = View.VISIBLE
                } else {
                    mediaLinearlayout.visibility = View.GONE
                }
            }

            infoTextView.text = "${json.instance} / ${json.timeline}"

            iconInfoImageView.setImageDrawable(getTLIcon(context, json))

        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun startBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    fun getTLIcon(context: Context, json: TimelineJSON): Drawable? {
        return when (json.timeline) {
            "home" -> context.getDrawable(R.drawable.ic_home_24px)
            "local" -> context.getDrawable(R.drawable.ic_directions_subway_24px)
            "notification" -> context.getDrawable(R.drawable.ic_notifications_24px)
            else -> context.getDrawable(R.drawable.ic_home_24px)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.adapter_timeline_cardview)
        val contentTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_content)
        val userNameTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_username)
        val createAtTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_createat)
        val infoTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_info)
        val iconInfoImageView = itemView.findViewById<ImageView>(R.id.adapter_timeline_icon_info)
        val media1 = itemView.findViewById<ImageView>(R.id.adapter_timeline_media_1)
        val media2 = itemView.findViewById<ImageView>(R.id.adapter_timeline_media_2)
        val media3 = itemView.findViewById<ImageView>(R.id.adapter_timeline_media_3)
        val media4 = itemView.findViewById<ImageView>(R.id.adapter_timeline_media_4)
        val mediaLinearlayout =
            itemView.findViewById<LinearLayout>(R.id.adapter_timeline_media_linearlayout)
        val favTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_fav)
        val boostTextView = itemView.findViewById<TextView>(R.id.adapter_timeline_boost)
        val avatarImageView = itemView.findViewById<ImageView>(R.id.adapter_timeline_avatar)
    }
}