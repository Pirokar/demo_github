package com.sequenia.threads.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by yuri on 07.07.2016.
 */
public class GalleryDecorator extends RecyclerView.ItemDecoration {
    private int offset;

    public GalleryDecorator(int offset) {
        this.offset = offset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset, view.getContext().getResources().getDisplayMetrics());
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top += size;
            outRect.bottom += size / 2;
            outRect.left += size;
            outRect.right += size / 2 - size / 4;
            return;
        }
        if (parent.getChildAdapterPosition(view) == 1) {
            outRect.top += size;
            outRect.bottom += size / 2;
            outRect.left += size / 2 + size / 6;
            outRect.right += size / 2 + size / 6;
            return;
        }
        if (parent.getChildAdapterPosition(view) == 2) {
            outRect.top += size;
            outRect.bottom += size / 2;
            outRect.left += size / 2 - size / 4;
            outRect.right += size;
            return;
        }

        if (parent.getChildAdapterPosition(view) % 3 == 0) {
            outRect.left += size;
            outRect.right += size / 2 - size / 4;
            outRect.top += size / 2;
            outRect.bottom += size / 2;
        } else if ((parent.getChildAdapterPosition(view) - 1) % 3 == 0) {
            outRect.left += size / 2 + size / 6;
            outRect.right += size / 2 + size / 6;
            outRect.top += size / 2;
            outRect.bottom += size / 2;
        } else {
            outRect.left += size / 2 - size / 4;
            outRect.right += size;
            outRect.top += size / 2;
            outRect.bottom += size / 2;
        }
    }
}
