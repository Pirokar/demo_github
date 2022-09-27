package im.threads.ui.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.ui.adapters.BottomGalleryAdapter;
import im.threads.ui.models.BottomGalleryItem;

public final class BottomGallery extends FrameLayout {

    private RecyclerView mRecyclerView;

    public BottomGallery(Context context) {
        super(context);
        init();
    }

    public BottomGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_gallery, this, true);
        mRecyclerView = findViewById(R.id.bottom_gallery_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    public void setImages(List<Uri> images, BottomGalleryAdapter.OnChooseItemsListener listener) {
        List<BottomGalleryItem> items = new ArrayList<>();
        for (Uri str : images) {
            BottomGalleryItem item = new BottomGalleryItem(false, str);
            items.add(item);
        }
        mRecyclerView.setAdapter(new BottomGalleryAdapter(items, listener));
    }
}
