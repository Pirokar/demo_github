package com.sequenia.threads;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by yuri on 07.07.2016.
 */
public class BucketGalleryDecorator extends RecyclerView.ItemDecoration {
    private int offset;

    public BucketGalleryDecorator(int offset) {
        this.offset = offset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset, view.getContext().getResources().getDisplayMetrics());
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.bottom += size/2;
            outRect.left += size;
            outRect.right += size / 2;
            return;
        }
        if (parent.getChildAdapterPosition(view) == 1) {
            outRect.bottom += size/2;
            outRect.left += size / 2;
            outRect.right += size;
            return;
        }

        if (parent.getChildAdapterPosition(view) % 2 == 0) {//not even
            outRect.top += size/2;
            outRect.bottom += size/2;
            outRect.left += size ;
            outRect.right += size/2;
        }else {
            outRect.top += size/2;
            outRect.bottom += size/2;
            outRect.left += size/2 ;
            outRect.right += size;
        }
    }
}
