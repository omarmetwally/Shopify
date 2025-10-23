import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class CustomLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
        return 100f / displayMetrics.densityDpi // Adjust the value to slow down the speed
    }
}



class CustomLinearLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller = CustomLinearSmoothScroller(recyclerView?.context!!)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}
