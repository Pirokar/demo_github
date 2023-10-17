package im.threads.ui.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import im.threads.BuildConfig
import im.threads.R
import im.threads.business.formatters.RussianFormatSymbols
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.models.FileDescription
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.FileDownloader
import im.threads.business.utils.FileProvider
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.isImage
import im.threads.databinding.EccFragmentImageBinding
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ImageFragment : BaseFragment() {

    private lateinit var binding: EccFragmentImageBinding
    private val fileProvider: FileProvider by inject()
    private val style = getInstance().chatStyle

    private val rotateAnim = RotateAnimation(
        0f,
        360f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EccFragmentImageBinding.inflate(inflater, container, false)
        applyStyle()
        fillData()
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun applyStyle() = with(binding) {
        val context = requireActivity()
        if (sdf == null) {
            hoursMinutesSdf = SimpleDateFormat("hh:mm", Locale.getDefault())
            sdf = if (Locale.getDefault().language == "ru") {
                SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
            } else {
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            }
        }
        root.setBackgroundColor(ContextCompat.getColor(context, style.imagesScreenBackgroundColor))
        from.setTextColor(ContextCompat.getColor(context, style.imagesScreenAuthorTextColor))
        date.setTextColor(ContextCompat.getColor(context, style.imagesScreenDateTextColor))
        from.textSize = style.imagesScreenAuthorTextSize.toFloat()
        date.textSize = style.imagesScreenDateTextSize.toFloat()
    }

    private fun fillData() = with(binding) {
        val fileDescription = requireArguments().getParcelable<FileDescription>("fd")
            ?: throw IllegalStateException("you must provide filedescription")
        if (fileDescription.from != null && fileDescription.from != "null") {
            from.text = fileDescription.from
        } else {
            from.text = ""
        }
        if (fileDescription.timeStamp != 0L) {
            date.text = "${sdf?.format(fileDescription.timeStamp)} ${getString(R.string.ecc_in)} ${hoursMinutesSdf?.format(fileDescription.timeStamp)}"
        } else {
            date.text = ""
        }
        if (isImage(fileDescription)) {
            loadImage(fileDescription)
        }
    }

    private fun loadImage(fileDescription: FileDescription) {
        if (fileDescription.fileUri != null) {
            loadImageFromUri(fileDescription.fileUri)
        } else if (fileDescription.getPreviewFileDescription() != null) {
            loadPreview(fileDescription)
            loadFullImage(fileDescription)
        } else {
            binding.image.setImageResource(style.imagePlaceholder)
        }
    }

    private fun loadPreview(fileDescription: FileDescription) {
        binding.preview.isVisible = true
        val fileUri = if (getPreviewUri(fileDescription.getPreviewFileDescription())?.toString().isNullOrBlank()) {
            fileDescription.getPreviewFileDescription()?.downloadPath
        } else {
            getPreviewUri(fileDescription.getPreviewFileDescription())?.toString()
        }
        get()
            .load(fileUri)
            .autoRotateWithExif(true)
            .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE)
            .errorDrawableResourceId(style.imagePlaceholder)
            .into(binding.preview)
    }

    private fun loadFullImage(fileDescription: FileDescription) {
        showLoader()
        val downloadPath = if (fileDescription.fileUri != null) {
            fileDescription.fileUri.toString()
        } else {
            fileDescription.downloadPath
        }
        get()
            .load(downloadPath)
            .autoRotateWithExif(true)
            .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE)
            .errorDrawableResourceId(style.imagePlaceholder)
            .callback(object : ImageLoader.ImageLoaderCallback {
                override fun onImageLoaded() {
                    stopAnimation()
                }

                override fun onImageLoadError() {
                    stopAnimation()
                }
            })
            .into(binding.image)
    }

    private fun loadImageFromUri(uri: Uri?) {
        binding.preview.isVisible = false
        get()
            .load(uri?.toString())
            .autoRotateWithExif(true)
            .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE)
            .errorDrawableResourceId(style.imagePlaceholder)
            .into(binding.image)
    }

    private fun showLoader() {
        binding.loader.isVisible = true
        initAnimation()
    }

    private fun initAnimation() {
        if (!BuildConfig.IS_ANIMATIONS_DISABLED.get()) {
            binding.loader.setImageResource(R.drawable.ecc_im_loading_themed)
            ColorsHelper.setTint(
                requireActivity(),
                binding.loader,
                style.incomingMessageLoaderColor
            )
            rotateAnim.duration = 3000
            rotateAnim.repeatCount = Animation.INFINITE
            binding.loader.animation = rotateAnim
            rotateAnim.start()
        }
    }

    private fun stopAnimation() {
        binding.preview.isVisible = false
        binding.loader.isVisible = false
        rotateAnim.cancel()
    }

    private fun getPreviewUri(fileDescription: FileDescription?): Uri? {
        fileDescription?.let {
            val outputFile = File(FileDownloader.getDownloadDir(requireContext()), FileUtils.generateFileName(fileDescription))
            return fileProvider.getUriForFile(requireContext(), outputFile)
        }
        return null
    }

    companion object {
        private var sdf: SimpleDateFormat? = null
        private var hoursMinutesSdf: SimpleDateFormat? = null

        @JvmStatic
        fun createImageFragment(fileDescription: FileDescription?): ImageFragment {
            val fragment = ImageFragment()
            val bundle = Bundle()
            bundle.putParcelable("fd", fileDescription)
            fragment.arguments = bundle
            return fragment
        }
    }
}
