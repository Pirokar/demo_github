package io.edna.threads.demo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.edna.threads.demo.R
import io.edna.threads.demo.business.ordinal
import io.edna.threads.demo.databinding.HolderDemoSamplesTextBinding
import io.edna.threads.demo.databinding.HolderDemoSamplesTitleBinding
import io.edna.threads.demo.databinding.HolderHorizontalLineBinding
import io.edna.threads.demo.ui.extenstions.inflateWithBinding
import io.edna.threads.demo.ui.models.DemoSamplesListItem
import io.edna.threads.demo.ui.models.DemoSamplesListItem.DIVIDER
import io.edna.threads.demo.ui.models.DemoSamplesListItem.TEXT
import io.edna.threads.demo.ui.models.DemoSamplesListItem.TITLE

class DemoSamplesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val list: MutableList<DemoSamplesListItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            DIVIDER.ordinal() -> {
                LineDividerHolder(inflater.inflateWithBinding(parent, R.layout.holder_horizontal_line))
            }
            TITLE.ordinal() -> {
                TitleHolder(inflater.inflateWithBinding(parent, R.layout.holder_demo_samples_title))
            }
            TEXT.ordinal() -> {
                TextHolder(inflater.inflateWithBinding(parent, R.layout.holder_demo_samples_text))
            }
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? DemoSamplesHolder)?.onBind(position)
    }

    override fun getItemCount() = list.count()

    override fun getItemViewType(position: Int) = list[position].ordinal()

    fun addItems(newItems: List<DemoSamplesListItem>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<DemoSamplesListItem>) {
        val diffResult = DiffUtil.calculateDiff(DemoSamplesDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class LineDividerHolder(val binding: HolderHorizontalLineBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder

    private inner class TitleHolder(val binding: HolderDemoSamplesTitleBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder {

        override fun onBind(position: Int) {
            (list[position] as? TITLE)?.let { binding.titleTextView.text = it.text }
        }
    }

    private inner class TextHolder(val binding: HolderDemoSamplesTextBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder {

        override fun onBind(position: Int) {
            (list[position] as? TEXT)?.let { binding.textTextView.text = it.text }
        }
    }

    private interface DemoSamplesHolder {
        fun onBind(position: Int) {}
    }
}
