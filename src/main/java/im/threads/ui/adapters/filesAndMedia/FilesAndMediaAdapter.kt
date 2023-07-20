package im.threads.ui.adapters.filesAndMedia

import android.view.ViewGroup
import androidx.core.util.ObjectsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.business.models.DateRow
import im.threads.business.models.FileAndMediaItem
import im.threads.business.models.FileDescription
import im.threads.business.models.MediaAndFileItem
import im.threads.business.utils.FileUtils.getFileName
import im.threads.ui.holders.EmptyViewHolder
import im.threads.ui.holders.FileAndMediaViewHolder
import im.threads.ui.holders.FilesDateStampHolder
import java.util.Calendar
import java.util.Locale

/** Адаптер для списка файлов, отправленных или полученных в чате */
@Suppress("NAME_SHADOWING")
internal class FilesAndMediaAdapter(
    filesList: List<FileDescription?>,
    private val onFileClick: OnFileClick,
    private val onDownloadFileClick: OnFileClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var list = ArrayList<MediaAndFileItem>()
    private var backup = ArrayList<MediaAndFileItem>()

    init {
        addItems(filesList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_ROW -> FilesDateStampHolder(parent)
            TYPE_FILE_AND_MEDIA_ROW -> FileAndMediaViewHolder(parent)
            else -> EmptyViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_DATE_ROW) {
            val dateStampHolder = holder as FilesDateStampHolder
            dateStampHolder.onBind(list[position].timeStamp)
        }
        if (getItemViewType(position) == TYPE_FILE_AND_MEDIA_ROW) {
            val fileAndMediaHolder = holder as FileAndMediaViewHolder
            val (fileDescription) = list[position] as FileAndMediaItem
            fileAndMediaHolder.onBind(
                fileDescription,
                {
                    val (fileItem) = list[fileAndMediaHolder.adapterPosition] as FileAndMediaItem
                    onFileClick.onFileClick(fileItem)
                }
            ) {
                val (fileItem) = list[fileAndMediaHolder.adapterPosition] as FileAndMediaItem
                onDownloadFileClick.onDownloadFileClick(fileItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is DateRow) {
            TYPE_DATE_ROW
        } else if (list[position] is FileAndMediaItem) {
            TYPE_FILE_AND_MEDIA_ROW
        } else {
            super.getItemViewType(position)
        }
    }

    /**
     * Проверяет количество элементов в списке на пустоту
     */
    fun isNotEmpty() = itemCount > 0

    /** Очищает текущий список, предварительно создавая его бэкап */
    fun backupAndClear() {
        backup = ArrayList(list)
        updateWithDiffUtil { list.clear() }
    }

    /**
     * Фильтрует список по заданному параметру
     *
     * @param filter строка для фильтрации
     */
    inline fun filter(filter: String?, onFiltered: () -> Unit) {
        var filter = filter
        if (filter == null) filter = ""
        val filteredItems = ArrayList<FileDescription>()
        for (item in backup) {
            if (item is FileAndMediaItem) {
                val (fileDescription, fileName) = item
                val lastPathSegment = fileDescription.fileUri?.lastPathSegment
                val lowercaseLastPath = lastPathSegment?.lowercase(Locale.getDefault())
                val lowercaseFilter = filter.lowercase(Locale.getDefault())
                val isContainsThisLocale = lowercaseLastPath?.contains(lowercaseFilter) == true

                if (isContainsThisLocale) {
                    filteredItems.add(fileDescription)
                } else if (fileDescription.incomingName != null) {
                    val lowercaseName = fileDescription.incomingName?.lowercase(Locale.getDefault())
                    if (lowercaseName?.contains(lowercaseFilter) == true) {
                        filteredItems.add(fileDescription)
                    }
                } else if (fileName.lowercase(Locale.getDefault()).contains(lowercaseFilter)) {
                    filteredItems.add(fileDescription)
                }
            }
        }

        updateWithDiffUtil {
            list = getMediaAndFileItems(filteredItems)
        }
        onFiltered()
    }

    /** Восстанавливает список из бэкапа */
    fun undoClear() {
        updateWithDiffUtil { list = ArrayList(backup) }
        backup.clear()
    }

    /**
     * Обновляет прогресс загрузки для файла
     *
     * @param fileDescription характеристики файла для обновление
     *     прогресса
     */
    fun updateProgress(fileDescription: FileDescription?) {
        for (i in list.indices) {
            if (list[i] is FileAndMediaItem) {
                val mediaItem = list[i] as FileAndMediaItem
                if (ObjectsCompat.equals(mediaItem.fileDescription, fileDescription)) {
                    val itemCopy = mediaItem.copy(
                        fileDescription = fileDescription!!,
                        fileName = mediaItem.fileName
                    )
                    list[i] = itemCopy
                    notifyItemChanged(i)
                }
            }
        }
    }

    /**
     * Показывает ошибку загрузки для файла
     *
     * @param fileDescription характеристики файла для отображения
     *     ошибки
     */
    fun onDownloadError(fileDescription: FileDescription?) {
        for (i in list.indices) {
            if (list[i] is FileAndMediaItem) {
                val (fileDescription1) = list[i] as FileAndMediaItem
                val itemViewType = getItemViewType(i)
                if (ObjectsCompat.equals(fileDescription1, fileDescription) &&
                    itemViewType == TYPE_FILE_AND_MEDIA_ROW
                ) {
                    fileDescription1.isDownloadError = true
                    notifyItemChanged(i)
                }
            }
        }
    }

    private fun addItems(fileDescriptionList: List<FileDescription?>) {
        list.addAll(getMediaAndFileItems(fileDescriptionList))
    }

    private fun getMediaAndFileItems(fileDescriptionList: List<FileDescription?>): ArrayList<MediaAndFileItem> {
        val result = ArrayList<MediaAndFileItem>()
        if (fileDescriptionList.isEmpty()) return result

        val current = Calendar.getInstance()
        val prev = Calendar.getInstance()
        fileDescriptionList[0]?.let { fd ->
            result.add(DateRow(fd.timeStamp))
            result.add(
                FileAndMediaItem(
                    fd,
                    if (fd.fileUri != null) {
                        getFileName(
                            fd.fileUri!!
                        )
                    } else {
                        ""
                    }
                )
            )
        }
        for (i in 1 until fileDescriptionList.size) {
            fileDescriptionList[i]?.let { fd ->
                current.timeInMillis = fd.timeStamp
                prev.timeInMillis = fileDescriptionList[i - 1]?.timeStamp ?: 0L
                result.add(
                    FileAndMediaItem(
                        fd,
                        if (fd.fileUri != null) {
                            getFileName(
                                fd.fileUri!!
                            )
                        } else {
                            ""
                        }
                    )
                )
                if (current[Calendar.DAY_OF_YEAR] != prev[Calendar.DAY_OF_YEAR]) {
                    result.add(DateRow(fd.timeStamp))
                }
            }
        }

        return result
    }

    private inline fun updateWithDiffUtil(changeItems: () -> Unit) {
        val oldItems = ArrayList(list)
        changeItems()
        val diffCallback = FilesAndMediaDiffUtil(oldItems, list)
        val diff = DiffUtil.calculateDiff(diffCallback)
        diff.dispatchUpdatesTo(this)
    }

    interface OnFileClick {
        /**
         * Описывает реакцию на нажатие
         *
         * @param fileDescription характеристики файла, на котором был
         *     произведен клик
         */
        fun onFileClick(fileDescription: FileDescription?)

        /**
         * Описывает реакцию на начало загрузки файла
         *
         * @param fileDescription характеристики файла, на котором был
         *     произведен клик
         */
        fun onDownloadFileClick(fileDescription: FileDescription?)
    }

    companion object {
        private const val TYPE_DATE_ROW = 1
        private const val TYPE_FILE_AND_MEDIA_ROW = 2
    }
}